/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.submissionservice.domain.repositories;

import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.models.files.InputStreamResource;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.SubmissionThrottling;
import tw.waterball.judgegirl.entities.submission.Verdict;
import tw.waterball.judgegirl.submissionservice.domain.usecases.dto.SubmissionQueryParams;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface SubmissionRepository {
    Optional<Submission> findOne(int studentId, String submissionId);

    void issueVerdictOfSubmission(String submissionId, Verdict verdict);

    List<Submission> findBySummaryJudgeStatus(JudgeStatus summaryJudgeStatus);

    Submission save(Submission submission);

    String saveZippedSubmittedCodesAndGetFileId(InputStreamResource inputStreamResource) throws IOException;

    Optional<FileResource> downloadZippedSubmittedCodes(String submissionId);

    List<Submission> find(SubmissionQueryParams params);

    Optional<SubmissionThrottling> findSubmissionThrottling(int problemId, int studentId);

    void saveSubmissionThrottling(SubmissionThrottling submissionThrottling);
}
