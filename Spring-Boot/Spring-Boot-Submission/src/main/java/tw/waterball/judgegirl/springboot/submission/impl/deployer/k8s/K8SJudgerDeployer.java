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

package tw.waterball.judgegirl.springboot.submission.impl.deployer.k8s;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.BatchV1Api;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1EnvVar;
import io.kubernetes.client.models.V1Job;
import io.kubernetes.client.models.V1JobBuilder;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.judgerapi.env.JudgerEnvVariables;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.ResourceSpec;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.springboot.configs.properties.JudgeGirlAmqpProps;
import tw.waterball.judgegirl.springboot.configs.properties.JudgeGirlJudgerProps;
import tw.waterball.judgegirl.springboot.configs.properties.JudgerServiceProps;
import tw.waterball.judgegirl.springboot.submission.configs.K8SDeployerAutoConfiguration;
import tw.waterball.judgegirl.submission.deployer.JudgerDeployer;

import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@ConditionalOnProperty(name = "judge-girl.judger.strategy",
        havingValue = K8SDeployerAutoConfiguration.STRATEGY)
@Component
public class K8SJudgerDeployer implements JudgerDeployer {
    private final BatchV1Api api;
    private final String jwtSecret;
    private final JudgerServiceProps.ProblemService problemServiceInstance;
    private final JudgerServiceProps.SubmissionService submissionServiceInstance;
    private final JudgeGirlAmqpProps amqpProps;
    private final JudgeGirlJudgerProps judgerProps;

    public K8SJudgerDeployer(BatchV1Api api, @Value("${jwt.secret}") String jwtSecret,
                             JudgerServiceProps.ProblemService problemServiceInstance,
                             JudgerServiceProps.SubmissionService submissionServiceInstance,
                             JudgeGirlAmqpProps amqpProps,
                             JudgeGirlJudgerProps judgerProps) {
        this.api = api;
        this.jwtSecret = jwtSecret;
        this.problemServiceInstance = problemServiceInstance;
        this.submissionServiceInstance = submissionServiceInstance;
        this.amqpProps = amqpProps;
        this.judgerProps = judgerProps;
    }

    @Override
    public void deployJudger(Problem problem, int studentId, Submission submission) {
        try {
            this.api.createNamespacedJob(judgerProps.getKubernetes().getNamespace(),
                    createJob(problem, studentId, submission), null, null, null);
            log.trace("[Judger Deployed] submissionId=\"{}\"", submission.getId());
        } catch (ApiException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    private V1Job createJob(Problem problem, int studentId, Submission submission) {
        LanguageEnv languageEnv = problem.getLanguageEnv(submission.getLanguageEnvName());
        ResourceSpec resourceSpec = languageEnv.getResourceSpec();

        // @formatter:off
        return new V1JobBuilder()
                .withKind("Job")
                .withNewMetadata()
                    .withName(format(judgerProps.getJob().getNameFormat(), submission.getId()))
                .endMetadata()
                .withNewSpec()
                    .withTtlSecondsAfterFinished(50)
                    .withNewTemplate()
                        .withNewSpec()
                            // pull from the private docker registry
                            // the secret must be created following the instructions: https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/
                            // TODO: currently don't support private registry
                            // .addToImagePullSecrets(new V1LocalObjectReference().name(judgerProps.getKubernetes().getImagePullSecret()))
                            .withRestartPolicy("Never")
                            .addNewContainer()
                                .withName(format(judgerProps.getContainer().getNameFormat(), submission.getId()))
                                .withImage(judgerProps.getImage().getName())
                                .withNewResources()
                                    .addToRequests("cpu", new Quantity(String.valueOf(resourceSpec.getCpu())))
                                    .addToLimits("nvidia.com/gpu", new Quantity(String.valueOf(resourceSpec.getGpu())))
                                .endResources()
                                .addAllToEnv(getEnvs(problem, studentId, submission))
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                    .withBackoffLimit(4)
                .endSpec()
                .build();
        // @formatter:on
    }

    private List<V1EnvVar> getEnvs(Problem problem, int studentId, Submission submission) {
        List<V1EnvVar> envs = new LinkedList<>();
        String traceId = MDC.get("traceId");
        JudgerEnvVariables.apply((env, value) -> envs.add(new V1EnvVar().name(env).value(value)),
                JudgerEnvVariables.Values.builder()
                        .studentId(studentId)
                        .problemId(problem.getId())
                        .submissionId(submission.getId())
                        .jwtSecret(jwtSecret)
                        .problemServiceInstance(problemServiceInstance)
                        .submissionServiceInstance(submissionServiceInstance)
                        .amqpVirtualHost(amqpProps.getVirtualHost())
                        .amqpHost(judgerProps.getAmqp().getHost())
                        .amqpPort(judgerProps.getAmqp().getPort())
                        .amqpUserName(amqpProps.getUsername())
                        .amqpPassword(amqpProps.getPassword())
                        .submissionsExchangeName(amqpProps.getSubmissionsExchangeName())
                        .verdictIssuedRoutingKeyFormat(
                                format(amqpProps.getVerdictIssuedRoutingKeyFormat(), "*"))
                        .traceId(traceId)
                        .build());
        return envs;
    }
}


