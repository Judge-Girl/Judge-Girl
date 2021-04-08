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

package tw.waterball.judgegirl.submissionservice.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tw.waterball.judgegirl.commons.helpers.EventBus;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.utils.functional.Otherwise;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.SubmissionThrottlingException;
import tw.waterball.judgegirl.entities.submission.verdict.Verdict;
import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.submissionservice.deployer.JudgerDeployer;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;

import javax.inject.Named;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.functional.Otherwise.empty;
import static tw.waterball.judgegirl.commons.utils.functional.Otherwise.of;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Named
@AllArgsConstructor
public class SubmitCodeUseCase {
    private final ThrottleSubmissionUseCase throttleSubmissionUseCase;
    private final SubmissionRepository submissionRepository;
    private final JudgerDeployer judgerDeployer;
    private final ProblemServiceDriver problemServiceDriver;
    private final EventBus eventBus;

    public void execute(SubmitCodeRequest request, SubmissionPresenter presenter) throws SubmissionThrottlingException {
        mayThrottleOnRequest(request);
        Problem problem = getProblem(request.problemId);

        Submission submission = saveSubmissionWithCodes(submission(request), request.fileResources);

        mayDeployJudgerIfNotJudged(request, problem, submission)
                .otherwise(eventBus::publish);

        presenter.setSubmission(submission);
    }

    private Submission submission(SubmitCodeRequest request) {
        Submission submission = new Submission(request.studentId, request.problemId, request.languageEnvName);
        submission.setBag(request.submissionBag);
        return submission;
    }

    private void mayThrottleOnRequest(SubmitCodeRequest request) throws SubmissionThrottlingException {
        log.info(request.toString());
        if (request.throttle) {
            throttleSubmissionUseCase.execute(request);
        }
    }

    private Problem getProblem(int problemId) {
        ProblemView problemView = problemServiceDriver.getProblem(problemId);
        return ProblemView.toEntity(problemView);
    }

    @SneakyThrows
    private Submission saveSubmissionWithCodes(Submission submission, List<FileResource> codes) {
        Submission saved = submissionRepository.saveSubmissionWithCodes(submission, codes);
        log.info("Saved submission: " + submission.getId());
        return saved;
    }

    private Otherwise<VerdictIssuedEvent> mayDeployJudgerIfNotJudged(SubmitCodeRequest request, Problem problem, Submission submission) {
        if (submission.isJudged()) {
            Verdict verdict = submission.getVerdict().orElseThrow();
            return of(new VerdictIssuedEvent(problem.getId(), submission.getStudentId(),
                    problem.getTitle(), submission.getId(), verdict, request.submissionBag));
        }
        judgerDeployer.deployJudger(problem, request.getStudentId(), submission);
        log.info("Completed: {}", request);
        return empty();
    }

}
