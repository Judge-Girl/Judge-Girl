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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tw.waterball.judgegirl.api.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.judger.infra.compile.ShellCompilerFactory;
import tw.waterball.judgegirl.judger.infra.testexecutor.CCSandboxTestcaseExecutorFactory;
import tw.waterball.judgegirl.judgerapi.env.JudgerEnvVariables;
import tw.waterball.judgegirl.plugins.Plugins;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPlugin;
import tw.waterball.judgegirl.plugins.api.PresetJudgeGirlPluginLocator;
import tw.waterball.judgegirl.problemapi.clients.ProblemApiClient;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.AmqpVerdictPublisher;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;

import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Provide different basic ways to construct a CCJudger.
 * (Either from env variables or customizing it)
 * @author - johnny850807@gmail.com (Waterball)
 */
public class DefaultCCJudgerFactory {

    @SneakyThrows
    public static CCJudger create(JudgerEnvVariables.Values values,
                                  String judgeWorkspaceLayoutYamlResourcePath,
                                  JudgeGirlPlugin... plugins) {

        return new CCJudger(
                new YAMLJudgerWorkspace(judgeWorkspaceLayoutYamlResourcePath),
                new PresetJudgeGirlPluginLocator(aggregateJudgeGirlPlugins(plugins)),
                problemApiClient(values),
                submissionApiClient(values),
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

    private static ProblemApiClient problemApiClient(JudgerEnvVariables.Values values) {
        return new ProblemApiClient(retrofitFactory(),
                values.getProblemServiceInstance().getScheme(),
                values.getProblemServiceInstance().getHost(),
                values.getProblemServiceInstance().getPort(),
                values.jwtToken);
    }

    private static SubmissionApiClient submissionApiClient(JudgerEnvVariables.Values values) {
        return new SubmissionApiClient(retrofitFactory(),
                values.getSubmissionServiceInstance().getScheme(),
                values.getSubmissionServiceInstance().getHost(),
                values.getSubmissionServiceInstance().getPort(),
                values.jwtToken);
    }

    private static RetrofitFactory retrofitFactory() {
        return new RetrofitFactory(objectMapper());
    }

    private static VerdictPublisher verdictPublisher(JudgerEnvVariables.Values values) {
        ConnectionFactory connectionFactory = connectionFactory(values);
        AmqpAdmin amqpAdmin = new RabbitAdmin(connectionFactory);
        AmqpTemplate amqpTemplate = new RabbitTemplate(connectionFactory);
        return new AmqpVerdictPublisher(amqpAdmin, amqpTemplate, objectMapper(),
                values.submissionExchangeName,
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
    private static ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
