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
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.judgerapi.env.JudgerEnvVariables;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.springboot.configs.properties.JudgeGirlAmqpProps;
import tw.waterball.judgegirl.springboot.configs.properties.JudgeGirlJudgerProps;
import tw.waterball.judgegirl.springboot.configs.properties.ServiceProps;
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
@ConditionalOnProperty(name = "judge-girl.judger.strategy",
        havingValue = DockerDeployerAutoConfiguration.STRATEGY)
@Component
public class DockerJudgerDeployer implements JudgerDeployer {
    private static final Logger logger = LogManager.getLogger(DockerJudgerDeployer.class);
    private final String jwtSecret;
    private final DockerClient dockerClient;
    private final ServiceProps.ProblemService problemServiceInstance;
    private final ServiceProps.SubmissionService submissionServiceInstance;
    private final JudgeGirlAmqpProps amqpProps;
    private final JudgeGirlJudgerProps judgerProps;

    public DockerJudgerDeployer(@Value("${jwt.secret}") String jwtSecret,
                                DockerClient dockerClient,
                                ServiceProps.ProblemService problemServiceInstance,
                                ServiceProps.SubmissionService submissionServiceInstance,
                                JudgeGirlAmqpProps amqpProps,
                                JudgeGirlJudgerProps judgerProps) {
        this.jwtSecret = jwtSecret;
        this.dockerClient = dockerClient;
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
                        .build());
        String containerName = format(judgerProps.getContainer().getNameFormat(), submission.getId());
        String containerId =
                dockerClient.createContainerCmd(judgerProps.getImage().getName())
                        .withName(containerName)
                        .withHostConfig(HostConfig
                                .newHostConfig()
                                .withNetworkMode(judgerProps.getDocker().getNetwork()))
                        .withEnv(envs)
                        .exec().getId();
        dockerClient.startContainerCmd(containerId).exec();
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
            logger.info("Remove all exited judger containers which are based on the image '{}', found: {}",
                    judgerProps.getImage().getName(), String.join(", ", removedNames));
        }
    }
}
