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

package tw.waterball.judgegirl.springboot.submission.impl.fake;

import lombok.Value;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.springboot.profiles.Dev;
import tw.waterball.judgegirl.submissionservice.ports.JudgerDeployer;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Dev
@Component
public class FakeJudgerDeployer implements JudgerDeployer {
    private Set<Deployment> deployments = new HashSet<>();

    @Override
    public void deployJudger(Problem problem, int studentId, Submission submission) {
        deployments.add(new Deployment(problem, studentId, submission));
    }

    public Set<Deployment> getDeployments() {
        return deployments;
    }

    public Optional<Deployment> getDeploymentByStudentId(int studentId) {
        return deployments.stream()
                .filter(d -> d.studentId == studentId)
                .findFirst();
    }

    @Value
    public static class Deployment {
        public Problem problemView;
        public int studentId;
        public Submission submission;
    }
}
