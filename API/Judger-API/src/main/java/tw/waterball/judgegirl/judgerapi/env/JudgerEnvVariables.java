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

package tw.waterball.judgegirl.judgerapi.env;

import lombok.Builder;
import lombok.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.waterball.judgegirl.api.ServiceInstance;

import java.util.Locale;

import static java.lang.Integer.parseInt;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface JudgerEnvVariables {
    Logger logger = LoggerFactory.getLogger(JudgerEnvVariables.class);
    // Judge specific envs
    String ENV_STUDENT_ID = "studentId";
    String ENV_PROBLEM_ID = "problemId";
    String ENV_SUBMISSION_ID = "submissionId";
    String ENV_JWT_SECRET = "jwt.secret";

    // Application Properties
    String ENV_PROBLEM_SVC_SCHEME = "judge-girl.client.problem-service.scheme";
    String ENV_PROBLEM_SVC_HOST = "judge-girl.client.problem-service.host";
    String ENV_PROBLEM_SVC_PORT = "judge-girl.client.problem-service.port";
    String ENV_SUBMISSION_SVC_SCHEME = "judge-girl.client.submission-service.scheme";
    String ENV_SUBMISSION_SVC_HOST = "judge-girl.client.submission-service.host";
    String ENV_SUBMISSION_SVC_PORT = "judge-girl.client.submission-service.port";
    String ENV_AMQP_VIRTUAL_HOST = "spring.rabbitmq.virtual-host";
    String ENV_AMQP_USERNAME = "spring.rabbitmq.username";
    String ENV_AMQP_PASSWORD = "spring.rabbitmq.password";
    String ENV_AMQP_HOST = "spring.rabbitmq.host";
    String ENV_AMQP_PORT = "spring.rabbitmq.port";
    String ENV_SUBMISSIONS_EXCHANGE_NAME = "judge-girl.amqp.submissions-exchange-name";
    String ENV_VERDICT_ISSUED_ROUTING_KEY_FORMAT = "judge-girl.amqp.verdict-issued-routing-key-format";

    // Observability
    String ENV_TRACE_ID = "trace-id";
    
    static void apply(Applier applier, Values values) {
        applier = handleDotsInEnvs(applier);

        applier.apply(ENV_STUDENT_ID, values.studentId);
        applier.apply(ENV_PROBLEM_ID, values.problemId);
        applier.apply(ENV_SUBMISSION_ID, values.submissionId);
        applier.apply(ENV_JWT_SECRET, values.jwtSecret);

        applier.apply(ENV_PROBLEM_SVC_HOST, values.problemServiceInstance.getHost());
        applier.apply(ENV_PROBLEM_SVC_PORT, values.problemServiceInstance.getPort());
        applier.apply(ENV_PROBLEM_SVC_SCHEME, values.problemServiceInstance.getScheme());

        applier.apply(ENV_SUBMISSION_SVC_HOST, values.submissionServiceInstance.getHost());
        applier.apply(ENV_SUBMISSION_SVC_PORT, values.submissionServiceInstance.getPort());
        applier.apply(ENV_SUBMISSION_SVC_SCHEME, values.submissionServiceInstance.getScheme());

        applier.apply(ENV_AMQP_VIRTUAL_HOST, values.amqpVirtualHost);
        applier.apply(ENV_AMQP_USERNAME, values.amqpUserName);
        applier.apply(ENV_AMQP_PASSWORD, values.amqpPassword);
        applier.apply(ENV_AMQP_HOST, values.amqpHost);
        applier.apply(ENV_AMQP_PORT, values.amqpPort);

        applier.apply(ENV_SUBMISSIONS_EXCHANGE_NAME, values.submissionsExchangeName);
        applier.apply(ENV_VERDICT_ISSUED_ROUTING_KEY_FORMAT, values.verdictIssuedRoutingKeyFormat);
        applier.apply(ENV_TRACE_ID, values.traceId);
    }

    // Since dots are usually not supported in env, convert the key in upper case format with underscore
    static Applier handleDotsInEnvs(Applier applier) {
        return (env, value) -> applier.apply(convertEnv(env), value);
    }

    static Values fromSystemEnvs() {
        System.getenv().forEach((key, value) -> System.out.println(key + "=" + value));

        return Values.builder()
                .studentId(parseInt(getenv(ENV_STUDENT_ID)))
                .problemId(parseInt(getenv(ENV_PROBLEM_ID)))
                .submissionId(getenv(ENV_SUBMISSION_ID))
                .jwtSecret(getenv(ENV_JWT_SECRET))
                .problemServiceInstance(
                        new ServiceInstance(
                                getenv(ENV_PROBLEM_SVC_SCHEME),
                                getenv(ENV_PROBLEM_SVC_HOST),
                                parseInt(getenv(ENV_PROBLEM_SVC_PORT))
                        )
                )
                .submissionServiceInstance(
                        new ServiceInstance(
                                getenv(ENV_SUBMISSION_SVC_SCHEME),
                                getenv(ENV_SUBMISSION_SVC_HOST),
                                parseInt(getenv(ENV_SUBMISSION_SVC_PORT))
                        ))
                .amqpVirtualHost(getenv(ENV_AMQP_VIRTUAL_HOST))
                .amqpUserName(getenv(ENV_AMQP_USERNAME))
                .amqpPassword(getenv(ENV_AMQP_PASSWORD))
                .amqpHost(getenv(ENV_AMQP_HOST))
                .amqpPort(parseInt(getenv(ENV_AMQP_PORT)))
                .submissionsExchangeName(getenv(ENV_SUBMISSIONS_EXCHANGE_NAME))
                .verdictIssuedRoutingKeyFormat(getenv(ENV_VERDICT_ISSUED_ROUTING_KEY_FORMAT))
                .traceId(getenv(ENV_TRACE_ID))
                .build();
    }


    static String getenv(String envKey) {
        String value = System.getenv(convertEnv(envKey));
        if (value == null) {
            throw new IllegalStateException("Environment variable '" + envKey + "' not found.");
        }
        return value;
    }

    static String convertEnv(String env) {
        Locale local = new Locale("en", "US");
        return env.toUpperCase(local).replaceAll("\\.", "_")
                .replaceAll("-", "_");
    }

    interface Applier {
        default void apply(String env, int value) {
            apply(env, String.valueOf(value));
        }

        void apply(String env, String value);
    }

    @Value
    @Builder
    class Values {
        public int studentId;
        public int problemId;
        public String submissionId;
        public String jwtSecret;
        public ServiceInstance problemServiceInstance;
        public ServiceInstance submissionServiceInstance;
        public String amqpVirtualHost;
        public String amqpUserName;
        public String amqpPassword;
        public String amqpHost;
        public int amqpPort;
        public String submissionsExchangeName;
        public String verdictIssuedRoutingKeyFormat;
        public String traceId;
    }

}
