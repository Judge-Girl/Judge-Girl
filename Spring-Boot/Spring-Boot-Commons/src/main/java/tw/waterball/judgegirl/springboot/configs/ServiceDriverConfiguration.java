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

package tw.waterball.judgegirl.springboot.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Interceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import tw.waterball.judgegirl.api.rest.RestTemplateFactory;
import tw.waterball.judgegirl.api.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.clients.RestProblemApiClient;
import tw.waterball.judgegirl.springboot.configs.properties.ServiceProps;
import tw.waterball.judgegirl.springboot.profiles.productions.ServiceDriver;
import tw.waterball.judgegirl.studentapi.clients.StudentApiClient;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.BagInterceptor;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;

import static tw.waterball.judgegirl.commons.token.TokenService.Identity.admin;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ServiceDriver
@Configuration
public class ServiceDriverConfiguration {
    private static final int DRIVER_STUDENT_ID = -10000000;
    public static final TokenService.Identity DRIVER = admin(DRIVER_STUDENT_ID);

    @Bean
    public RetrofitFactory retrofitFactory(ObjectMapper objectMapper,
                                           Interceptor[] interceptors) {
        return new RetrofitFactory(objectMapper, interceptors);
    }

    @Bean
    public RestTemplateFactory restTemplateFactory(ObjectMapper objectMapper) {
        return new RestTemplateFactory(objectMapper);
    }

    @Bean
    public ProblemServiceDriver problemServiceDriver(
            RestTemplateFactory restTemplateFactory,
            TokenService tokenService,
            ServiceProps.ProblemService problemServiceInstance,
            ClientHttpRequestInterceptor[] interceptors) {
        var restTemplate = restTemplateFactory.create(
                problemServiceInstance.getScheme(),
                problemServiceInstance.getHost(),
                problemServiceInstance.getPort(),
                interceptors);
        return new RestProblemApiClient(restTemplate,
                () -> tokenService.createToken(DRIVER).getToken());
    }

    @Bean
    public StudentServiceDriver studentServiceDriver(
            RetrofitFactory retrofitFactory,
            TokenService tokenService,
            ServiceProps.StudentService studentServiceInstance) {
        return new StudentApiClient(retrofitFactory,
                studentServiceInstance.getScheme(),
                studentServiceInstance.getHost(),
                studentServiceInstance.getPort(),
                () -> tokenService.createToken(DRIVER).getToken());

    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public SubmissionServiceDriver submissionServiceDriver(
            RetrofitFactory retrofitFactory,
            ObjectMapper objectMapper,
            TokenService tokenService,
            ServiceProps.SubmissionService submissionServiceInstance,
            BagInterceptor... bagInterceptors) {
        return new SubmissionApiClient(retrofitFactory,
                objectMapper,
                submissionServiceInstance.getScheme(),
                submissionServiceInstance.getHost(),
                submissionServiceInstance.getPort(),
                () -> tokenService.createToken(DRIVER).getToken(),
                bagInterceptors);
    }

}
