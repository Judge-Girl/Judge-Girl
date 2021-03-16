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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tw.waterball.judgegirl.commons.models.files.StreamingResource;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.submissionservice.deployer.JudgerDeployer;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class SubmitCodeUseCase {
    private final static Logger logger = LogManager.getLogger(SubmitCodeUseCase.class);

    private ThrottleSubmissionUseCase throttleSubmissionUseCase;
    private SubmissionRepository submissionRepository;
    private JudgerDeployer judgerDeployer;
    private ProblemServiceDriver problemServiceDriver;

    public SubmitCodeUseCase(ThrottleSubmissionUseCase throttleSubmissionUseCase,
                             JudgerDeployer judgerDeployer,
                             ProblemServiceDriver problemServiceDriver,
                             SubmissionRepository submissionRepository) {
        this.throttleSubmissionUseCase = throttleSubmissionUseCase;
        this.submissionRepository = submissionRepository;
        this.judgerDeployer = judgerDeployer;
        this.problemServiceDriver = problemServiceDriver;
    }

    public void execute(SubmitCodeRequest request, SubmissionPresenter presenter) throws IOException {
        logger.info(request);
        if (request.throttle) {
            throttleSubmissionUseCase.execute(request);
        }

        String submittedCodeFileId = zipAndSaveSubmittedCodesAndGetFileId(request);
        Problem problem = getProblem(request.problemId);
        Submission submission = new Submission(request.studentId, request.problemId, request.languageEnvName, submittedCodeFileId);
        submission.setProblemId(problem.getId());

        submission = submissionRepository.save(submission);
        logger.info("Saved submission: " + submission.getId());

        judgerDeployer.deployJudger(problem, request.getStudentId(), submission);
        logger.info("Completed: " + request);
        presenter.setSubmission(submission);
    }

    private Problem getProblem(int problemId) {
        ProblemView problemView = problemServiceDriver.getProblem(problemId);
        return ProblemView.toEntity(problemView);
    }

    private String zipAndSaveSubmittedCodesAndGetFileId(SubmitCodeRequest request) throws IOException {
        // TODO: refactor, the data-storing related code should be encapsulated in the repository
        String fileName = String.format("%d-%s-%d.zip", request.studentId, request.problemId,
                System.currentTimeMillis());
        InputStream zippedSubmittedCodesStream = ZipUtils.zipToStream(request.getFileResources());
        StreamingResource streamingResource = new StreamingResource(fileName, zippedSubmittedCodesStream);
        return submissionRepository.saveZippedSubmittedCodesAndGetFileId(streamingResource);
    }

}
