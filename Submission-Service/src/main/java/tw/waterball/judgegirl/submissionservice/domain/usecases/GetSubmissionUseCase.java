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

import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetSubmissionUseCase extends BaseSubmissionUseCase {

    public GetSubmissionUseCase(SubmissionRepository submissionRepository) {
        super(submissionRepository);
    }

    public void execute(Request request, SubmissionPresenter presenter) {
        Submission submission = doFindSubmission(request.studentId, request.submissionId);
        validateRequest(request, submission);
        presenter.setSubmission(submission);
    }

    private void validateRequest(Request request, Submission submission) {
        if (submission.getProblemId() != request.problemId) {
            throw new NotFoundException(submission.getId(), "submission");
        }
        if (!submission.getLanguageEnvName().equals(request.getLanguageEnvName())) {
            throw new NotFoundException(submission.getId(), "submission");
        }
    }

    @Value
    public static class Request {
        public int problemId;
        public String languageEnvName;
        public int studentId;
        public String submissionId;
    }
}
