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

package tw.waterball.judgegirl.springboot.submission.impl.k8s;

import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.BatchV1Api;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1EnvVar;
import io.kubernetes.client.models.V1Job;
import io.kubernetes.client.models.V1JobBuilder;
import io.kubernetes.client.models.V1LocalObjectReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.problem.JudgeSpec;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.stubs.Stubs;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.profiles.productions.K8S;
import tw.waterball.judgegirl.springboot.submission.SubmissionServiceApplication;
import tw.waterball.judgegirl.springboot.utils.ApplicationContextUtils;
import tw.waterball.judgegirl.submissionservice.ports.JudgerDeployer;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@K8S
@PropertySource("classpath:judger-deploy.properties")
@Component
public class K8SJudgerDeployer implements JudgerDeployer {
    private final String judgerJobNameFormat;
    private final String judgerImageName;
    private final String judgerContainerNameFormat;
    private final String judgerImagePullSecret;
    private BatchV1Api api;

    public K8SJudgerDeployer(BatchV1Api api,
                             @Value("${judger-deploy.job.name.format}") String judgerJobNameFormat,
                             @Value("${judger-deploy.image.name}") String judgerImageName,
                             @Value("${judger-deploy.container.name.format}") String judgerContainerNameFormat,
                             @Value("${judger-deploy.image-pull-secret}") String judgerImagePullSecret) {
        this.judgerJobNameFormat = judgerJobNameFormat;
        this.judgerImageName = judgerImageName;
        this.judgerContainerNameFormat = judgerContainerNameFormat;
        this.judgerImagePullSecret = judgerImagePullSecret;
        this.api = api;
    }

    public static void main(String[] args) {
        ApplicationContext context = ApplicationContextUtils.setupSpringApplicationContext(
                new String[]{Profiles.DEV, Profiles.SERVICE_DRIVER, Profiles.K8S}, SubmissionServiceApplication.class);

        JudgerDeployer judgerDeployer = context.getBean(JudgerDeployer.class);
        judgerDeployer.deployJudger(Stubs.problemTemplateBuilder().build(), 1434,
                new Submission("1", 1, 1, ""));
    }

    @Override
    public void deployJudger(Problem problem, int studentId, Submission submission) {
        try {
            this.api.createNamespacedJob("default",
                    createJob(problem, studentId, submission), null, null, null);
        } catch (ApiException e) {
            throw new RuntimeException(e); // TODO
        }
    }

    private V1Job createJob(Problem problem, int studentId, Submission submission) {
        JudgeSpec judgeSpec = problem.getJudgeSpec();
        return new V1JobBuilder()
                .withKind("Job")
                .withNewMetadata()
                .withName(String.format(judgerJobNameFormat, submission.getId()))
                .endMetadata()
                .withNewSpec()
                .withTtlSecondsAfterFinished(50)
                .withNewTemplate()
                .withNewSpec()
                // pull from the private docker registry
                // the secret must be created following the instructions: https://kubernetes.io/docs/tasks/configure-pod-container/pull-image-private-registry/
                .addToImagePullSecrets(new V1LocalObjectReference().name(judgerImagePullSecret))
                .addNewContainer()
                .withName(String.format(judgerContainerNameFormat, submission.getId()))
                .withImage(judgerImageName)
                .withNewResources()
                .addToRequests("cpu", new Quantity(String.valueOf(judgeSpec.getCpu())))
                .addToLimits("nvidia.com/gpu", new Quantity(String.valueOf(judgeSpec.getGpu())))
                .endResources()
                .addToEnv(new V1EnvVar().name("submissionId").value(String.valueOf(submission.getId())))
                .addToEnv(new V1EnvVar().name("problemId").value(String.valueOf(problem.getId())))
                .addToEnv(new V1EnvVar().name("studentId").value(String.valueOf(studentId)))
                .endContainer()
                .withRestartPolicy("Never")
                .endSpec()
                .endTemplate()
                .withBackoffLimit(4)
                .endSpec()
                .build();
    }
}


