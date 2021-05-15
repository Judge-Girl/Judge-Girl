/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.judger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import tw.waterball.judgegirl.api.rest.RestTemplateFactory;
import tw.waterball.judgegirl.api.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.commons.token.jwt.JwtTokenService;
import tw.waterball.judgegirl.judger.infra.compile.ShellCompilerFactory;
import tw.waterball.judgegirl.judger.infra.testexecutor.CCSandboxTestcaseExecutorFactory;
import tw.waterball.judgegirl.judgerapi.env.JudgerEnvVariables;
import tw.waterball.judgegirl.plugins.Plugins;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPlugin;
import tw.waterball.judgegirl.plugins.api.PresetJudgeGirlPluginLocator;
import tw.waterball.judgegirl.problemapi.clients.ProblemRestApiClient;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.springboot.amqp.AmqpVerdictPublisher;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.admin;
import static tw.waterball.judgegirl.commons.utils.DateUtils.afterCurrentTime;

/**
 * Provide different basic ways to construct a CCJudger.
 * (Either from env variables or customizing it)
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class DefaultCCJudgerFactory {
    private static final int JUDGER_STUDENT_ID = -999999;

    @SneakyThrows
    public static CCJudger create(JudgerEnvVariables.Values values,
                                  String judgeWorkspaceLayoutYamlResourcePath,
                                  JudgeGirlPlugin... plugins) {
        JwtTokenService jwtTokenService = new JwtTokenService(values.jwtSecret, afterCurrentTime(10, TimeUnit.DAYS).getTime());
        String token = jwtTokenService.createToken(admin(JUDGER_STUDENT_ID)).getToken();
        return new CCJudger(
                new YAMLJudgerWorkspace(judgeWorkspaceLayoutYamlResourcePath),
                new PresetJudgeGirlPluginLocator(aggregateJudgeGirlPlugins(plugins)),
                problemRestApiClient(values, token),
                submissionApiClient(values, token),
                verdictPublisher(values),
                new ShellCompilerFactory(),
                new CCSandboxTestcaseExecutorFactory()
        );
    }


    @SneakyThrows
    public static CCJudger create(String judgeWorkspaceLayoutYamlResourcePath,
                                  ProblemServiceDriver problemServiceDriver,
                                  SubmissionServiceDriver submissionServiceDriver,
                                  VerdictPublisher verdictPublisher,
                                  JudgeGirlPlugin... plugins) {
        return new CCJudger(
                new YAMLJudgerWorkspace(judgeWorkspaceLayoutYamlResourcePath),
                new PresetJudgeGirlPluginLocator(aggregateJudgeGirlPlugins(plugins)),
                problemServiceDriver,
                submissionServiceDriver,
                verdictPublisher,
                new ShellCompilerFactory(),
                new CCSandboxTestcaseExecutorFactory()
        );
    }

    private static List<JudgeGirlPlugin> aggregateJudgeGirlPlugins(JudgeGirlPlugin... plugins) {
        List<JudgeGirlPlugin> allPlugins = new LinkedList<>();
        allPlugins.addAll(Plugins.getDefaultPlugins());
        allPlugins.addAll(asList(plugins));
        return allPlugins;
    }

    private static ProblemRestApiClient problemRestApiClient(JudgerEnvVariables.Values values, String token) {
        return new ProblemRestApiClient(restTemplateFactory(),
                values.getProblemServiceInstance().getScheme(),
                values.getProblemServiceInstance().getHost(),
                values.getProblemServiceInstance().getPort(),
                () -> token);
    }


    private static SubmissionApiClient submissionApiClient(JudgerEnvVariables.Values values, String token) {
        return new SubmissionApiClient(retrofitFactory(),
                values.getSubmissionServiceInstance().getScheme(),
                values.getSubmissionServiceInstance().getHost(),
                values.getSubmissionServiceInstance().getPort(),
                () -> token);
    }

    private static RetrofitFactory retrofitFactory() {
        return new RetrofitFactory(objectMapper());
    }

    private static RestTemplateFactory restTemplateFactory() {
        return new RestTemplateFactory(objectMapper());
    }

    private static VerdictPublisher verdictPublisher(JudgerEnvVariables.Values values) {
        ConnectionFactory connectionFactory = connectionFactory(values);
        AmqpAdmin amqpAdmin = new RabbitAdmin(connectionFactory);
        RabbitTemplate amqpTemplate = new RabbitTemplate(connectionFactory);
        ObjectMapper objectMapper = objectMapper();
        amqpTemplate.setMessageConverter(new Jackson2JsonMessageConverter(objectMapper));
        return new AmqpVerdictPublisher(amqpAdmin, amqpTemplate,
                values.verdictExchangeName,
                values.verdictIssuedRoutingKeyFormat);
    }

    private static ConnectionFactory connectionFactory(JudgerEnvVariables.Values values) {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setVirtualHost(values.amqpVirtualHost);
        connectionFactory.setHost(values.amqpHost);
        connectionFactory.setPort(values.amqpPort);
        connectionFactory.setUsername(values.amqpUserName);
        connectionFactory.setPassword(values.amqpPassword);
        return connectionFactory;
    }

    // TODO the objectMapper implementation should be injected
    public static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
