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

package tw.waterball.judgegirl.submission.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.utils.functional.Otherwise;
import tw.waterball.judgegirl.primitives.EventBus;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottlingException;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.submission.deployer.JudgerDeployer;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;

import javax.inject.Named;
import java.util.List;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.functional.Otherwise.empty;
import static tw.waterball.judgegirl.commons.utils.functional.Otherwise.of;
import static tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent.liveSubmission;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Named
@AllArgsConstructor
public class SubmitCodeUseCase implements VerdictIssuedEventHandler {
    private final ThrottleSubmissionUseCase throttleSubmissionUseCase;
    private final SubmissionRepository submissionRepository;
    private final JudgerDeployer judgerDeployer;
    private final ProblemServiceDriver problemService;
    private final EventBus eventBus;

    public void execute(SubmitCodeRequest request, SubmissionPresenter presenter) throws SubmissionThrottlingException {
        log.info("[Submit Code] {}", request.toString());
        mayThrottleOnRequest(request);
        Problem problem = getProblem(request.problemId);

        Submission submission = saveSubmissionWithCodes(submission(request), request.fileResources);

        mayDeployJudgerAndPublishEvents(problem, submission);

        presenter.setSubmission(submission);
    }

    public void execute(Submission submission) throws SubmissionThrottlingException {
        Problem problem = getProblem(submission.getProblemId());

        mayDeployJudgerAndPublishEvents(problem, saveSubmission(submission));
    }

    private void mayDeployJudgerAndPublishEvents(Problem problem, Submission submission) {
        mayDeployJudgerIfNotJudged(problem, submission)
                .otherwise(eventBus::publish);

        eventBus.publish(liveSubmission(submission));
    }

    private Submission submission(SubmitCodeRequest request) {
        Submission submission = new Submission(request.studentId, request.problemId, request.languageEnvName);
        submission.setBag(request.submissionBag);
        return submission;
    }

    private void mayThrottleOnRequest(SubmitCodeRequest request) throws SubmissionThrottlingException {
        if (request.throttle) {
            throttleSubmissionUseCase.execute(request);
        }
    }

    private Problem getProblem(int problemId) {
        return problemService.getProblem(problemId)
                .map(ProblemView::toEntity)
                .orElseThrow(() -> notFound(Problem.class).id(problemId));
    }

    @SneakyThrows
    private Submission saveSubmissionWithCodes(Submission submission, List<FileResource> codes) {
        Submission saved = submissionRepository.saveSubmissionWithCodes(submission, codes);
        log.trace("[Submission Saved] submissionId=\"{}\"", saved.getId());
        return saved;
    }

    @SneakyThrows
    private Submission saveSubmission(Submission submission) {
        Submission saved = submissionRepository.save(submission);
        log.info("Saved submission: " + submission.getId());
        return saved;
    }

    private Otherwise<VerdictIssuedEvent> mayDeployJudgerIfNotJudged(Problem problem, Submission submission) {
        if (submission.isJudged()) {
            Verdict verdict = submission.mayHaveVerdict().orElseThrow();
            return of(new VerdictIssuedEvent(problem.getId(), problem.getTitle(), submission.getStudentId(),
                    submission.getId(), verdict, submission.getSubmissionTime(), submission.getBag()));
        }
        judgerDeployer.deployJudger(problem, submission.getStudentId(), submission);

        log.trace("[Judger Deployed] submissionId=\"{}\"", submission.getId());
        return empty();
    }

    @Override
    public void handle(VerdictIssuedEvent event) {
        log.trace("[Verdict Issued] submissionId=\"{}\"", event.getSubmissionId());

        Verdict verdict = event.getVerdict();
        submissionRepository.issueVerdict(event.getSubmissionId(), verdict);
    }
}
