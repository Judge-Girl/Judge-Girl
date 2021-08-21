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
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.judgerapi.env.JudgerEnvVariables;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.springboot.configs.properties.JudgeGirlAmqpProps;
import tw.waterball.judgegirl.springboot.configs.properties.JudgeGirlJudgerProps;
import tw.waterball.judgegirl.springboot.configs.properties.JudgerServiceProps;
import tw.waterball.judgegirl.springboot.submission.configs.DockerDeployerAutoConfiguration;
import tw.waterball.judgegirl.submission.deployer.JudgerDeployer;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.singletonList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@ConditionalOnProperty(name = "judge-girl.judger.strategy",
        havingValue = DockerDeployerAutoConfiguration.STRATEGY)
@Component
public class DockerJudgerDeployer implements JudgerDeployer {
    // Since Docker Client processes each incoming request in a FIFO manner,
    // it will become a bottleneck during a massive workload;
    // Using the taskScheduler to run it in the background can avoid client's TIMEOUT.
    private final ThreadPoolTaskScheduler taskScheduler;
    private final String jwtSecret;
    private final DockerClient dockerClient;
    private final JudgerServiceProps.ProblemService problemServiceInstance;
    private final JudgerServiceProps.SubmissionService submissionServiceInstance;
    private final JudgeGirlAmqpProps amqpProps;
    private final JudgeGirlJudgerProps judgerProps;

    public DockerJudgerDeployer(@Value("${jwt.secret}") String jwtSecret,
                                DockerClient dockerClient,
                                JudgerServiceProps.ProblemService problemServiceInstance,
                                JudgerServiceProps.SubmissionService submissionServiceInstance,
                                JudgeGirlAmqpProps amqpProps,
                                JudgeGirlJudgerProps judgerProps,
                                ThreadPoolTaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
        this.jwtSecret = jwtSecret;
        this.dockerClient = dockerClient;
        this.problemServiceInstance = problemServiceInstance;
        this.submissionServiceInstance = submissionServiceInstance;
        this.amqpProps = amqpProps;
        this.judgerProps = judgerProps;
    }

    @Override
    public void deployJudger(Problem problem, int studentId, Submission submission) {
        taskScheduler.execute(() -> {
            List<String> envs = getEnvs(problem, studentId, submission);
            String containerName = format(judgerProps.getContainer().getNameFormat(), submission.getId());
            HostConfig hostConfig = hostConfig();
            String containerId = createContainer(envs, containerName, hostConfig);
            dockerClient.startContainerCmd(containerId).exec();
            log.trace("[Judger Deployed] submissionId=\"{}\"", submission.getId());
        });
    }

    private List<String> getEnvs(Problem problem, int studentId, Submission submission) {
        List<String> envs = new LinkedList<>();
        String traceId = MDC.get("traceId");
        JudgerEnvVariables.apply((env, value) -> envs.add(env + "=" + value),
                JudgerEnvVariables.Values.builder()
                        .studentId(studentId)
                        .problemId(problem.getId())
                        .submissionId(submission.getId())
                        .jwtSecret(jwtSecret)
                        .problemServiceInstance(problemServiceInstance)
                        .submissionServiceInstance(submissionServiceInstance)
                        .amqpVirtualHost(amqpProps.getVirtualHost())
                        .amqpHost(amqpProps.getHost())
                        .amqpPort(amqpProps.getPort())
                        .amqpUserName(amqpProps.getUsername())
                        .amqpPassword(amqpProps.getPassword())
                        .submissionsExchangeName(amqpProps.getSubmissionsExchangeName())
                        .verdictIssuedRoutingKeyFormat(
                                format(amqpProps.getVerdictIssuedRoutingKeyFormat(), "*"))
                        .traceId(traceId)
                        .build());
        return envs;
    }

    private HostConfig hostConfig() {
        boolean isLogVolumeHostEnable = judgerProps.getDocker().isLogVolumeEnable();
        String logVolumeHostPath = judgerProps.getDocker().getLogVolumeHost();
        HostConfig hostConfig = HostConfig
                .newHostConfig()
                .withNetworkMode(judgerProps.getDocker().getNetwork());
        if (isLogVolumeHostEnable) {
            hostConfig.withBinds(new Bind(logVolumeHostPath,
                    new Volume(/* TODO: enhance; hard-coded path */"/judger-home/log")));
        }
        return hostConfig;
    }

    private String createContainer(List<String> envs, String containerName, HostConfig hostConfig) {
        return dockerClient.createContainerCmd(judgerProps.getImage().getName())
                .withName(containerName)
                .withHostConfig(hostConfig)
                .withEnv(envs)
                .exec().getId();
    }

    @Scheduled(initialDelay = 5000, fixedDelayString = "${judge-girl.judger.docker.dockerRemovalIntervalInMs}")
    private void removeAllExitedJudgerContainers() {
        List<Container> containers = dockerClient.listContainersCmd()
                .withAncestorFilter(singletonList(judgerProps.getImage().getName()))
                .withStatusFilter(singletonList("exited")).exec();
        containers.stream().map(Container::getId)
                .map(dockerClient::removeContainerCmd)
                .forEach(RemoveContainerCmd::exec);
        Set<String> removedNames = containers.stream()
                .flatMap(c -> Arrays.stream(c.getNames()))
                .map(name -> name.substring(1))
                .collect(Collectors.toSet()); // remove the beginning slash '/'
        if (!removedNames.isEmpty()) {
            log.trace("Remove all exited judger containers which are based on the image '{}', found: {}",
                    judgerProps.getImage().getName(), String.join(", ", removedNames));
        }
    }
}
