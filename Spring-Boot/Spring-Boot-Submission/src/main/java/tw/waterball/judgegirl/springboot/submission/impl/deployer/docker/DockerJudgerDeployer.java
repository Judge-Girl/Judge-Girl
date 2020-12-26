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

package tw.waterball.judgegirl.springboot.submission.impl.deployer.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.judgerapi.env.JudgerEnvVariables;
import tw.waterball.judgegirl.springboot.configs.properties.JudgeGirlAmqpProps;
import tw.waterball.judgegirl.springboot.configs.properties.JudgeGirlJudgerProps;
import tw.waterball.judgegirl.springboot.configs.properties.ServiceProps;
import tw.waterball.judgegirl.submissionservice.ports.JudgerDeployer;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class DockerJudgerDeployer implements JudgerDeployer {
    private DockerClient dockerClient;
    private ScheduledExecutorService scheduler;
    private ServiceProps.ProblemService problemServiceInstance;
    private ServiceProps.SubmissionService submissionServiceInstance;
    private JudgeGirlAmqpProps amqpProps;
    private JudgeGirlJudgerProps judgerProps;

    public DockerJudgerDeployer(DockerClient dockerClient,
                                ScheduledExecutorService scheduler,
                                ServiceProps.ProblemService problemServiceInstance,
                                ServiceProps.SubmissionService submissionServiceInstance,
                                JudgeGirlAmqpProps amqpProps,
                                JudgeGirlJudgerProps judgerProps) {
        this.dockerClient = dockerClient;
        this.scheduler = scheduler;
        this.problemServiceInstance = problemServiceInstance;
        this.submissionServiceInstance = submissionServiceInstance;
        this.amqpProps = amqpProps;
        this.judgerProps = judgerProps;
    }

    @Override
    public void deployJudger(Problem problem, int studentId, Submission submission) {
        List<String> envs = new LinkedList<>();
        JudgerEnvVariables.apply((env, value) -> envs.add(env + "=" + value),
                JudgerEnvVariables.Values.builder()
                        .studentId(studentId)
                        .problemId(problem.getId())
                        .submissionId(submission.getId())
                        .jwtToken(judgerProps.getJwtToken())
                        .problemServiceInstance(problemServiceInstance)
                        .submissionServiceInstance(submissionServiceInstance)
                        .amqpVirtualHost(amqpProps.getVirtualHost())
                        .amqpHost(amqpProps.getHost())
                        .amqpPort(amqpProps.getPort())
                        .amqpUserName(amqpProps.getUsername())
                        .amqpPassword(amqpProps.getPassword())
                        .submissionExchangeName(amqpProps.getSubmissionExchangeName())
                        .verdictIssuedRoutingKeyFormat(
                                format(amqpProps.getVerdictIssuedRoutingKeyFormat(), "*"))
                        .build());
        String containerId =
                dockerClient.createContainerCmd(judgerProps.getImage().getName())
                        .withName(format(judgerProps.getContainer().getNameFormat(), submission.getId()))
                        .withHostConfig(HostConfig
                                .newHostConfig()
                                .withNetworkMode(judgerProps.getDocker().getNetwork()))
                        .withEnv(envs)
                        .exec().getId();

        dockerClient.startContainerCmd(containerId).exec();
    }

    @PostConstruct
    public void startJudgerAutoRemoval() {
        scheduler.scheduleAtFixedRate(this::removeAllExitedJudgerContainers,
                60, 10, TimeUnit.SECONDS);
    }

    private void removeAllExitedJudgerContainers() {
        dockerClient.listContainersCmd()
                .withNameFilter(singletonList(judgerProps.getImage().getName()))
                .withStatusFilter(singletonList("exited")).exec().stream()
                .map(Container::getId).forEach(dockerClient::removeContainerCmd);
    }
}
