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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.api.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.problemapi.clients.ProblemApiClient;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.springboot.configs.properties.ServiceProps;
import tw.waterball.judgegirl.springboot.profiles.productions.ServiceDriver;
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
    public final static String BEAN_NAME_SUBMISSION_BAG = "bean-name-submission-bag";

    @Bean
    public RetrofitFactory retrofitFactory(ObjectMapper objectMapper) {
        return new RetrofitFactory(objectMapper);
    }

    @Bean
    public ProblemServiceDriver problemServiceDriver(
            RetrofitFactory retrofitFactory,
            TokenService tokenService,
            ServiceProps.ProblemService problemServiceInstance,
            @Value("${judge-girl.client.problem-service.studentId}") int studentId) {
        String adminToken = tokenService.createToken(admin(studentId)).getToken();

        return new ProblemApiClient(retrofitFactory,
                problemServiceInstance.getScheme(),
                problemServiceInstance.getHost(),
                problemServiceInstance.getPort(), adminToken);
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    public SubmissionServiceDriver submissionServiceDriver(
            RetrofitFactory retrofitFactory,
            TokenService tokenService,
            ServiceProps.SubmissionService submissionServiceInstance,
            @Value("${judge-girl.client.submission-service.studentId}") int studentId,
            BagInterceptor... bagInterceptors) {
        String adminToken = tokenService.createToken(admin(studentId)).getToken();
        return new SubmissionApiClient(retrofitFactory,
                submissionServiceInstance.getScheme(),
                submissionServiceInstance.getHost(),
                submissionServiceInstance.getPort(), adminToken, bagInterceptors);
    }

}
