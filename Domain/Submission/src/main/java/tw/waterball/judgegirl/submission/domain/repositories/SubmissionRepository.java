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

package tw.waterball.judgegirl.submission.domain.repositories;

import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.models.files.StreamingResource;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottling;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.submission.domain.usecases.dto.SubmissionQueryParams;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface SubmissionRepository {
    List<Submission> findByProblemIdAndJudgeStatus(int problemId, JudgeStatus judgeStatus);

    List<Submission> query(SubmissionQueryParams params);

    Optional<Submission> findById(String submissionId);

    Optional<Submission> findOne(int studentId, String submissionId);

    void issueVerdictOfSubmission(String submissionId, Verdict verdict);

    List<Submission> findBySummaryJudgeStatus(JudgeStatus summaryJudgeStatus);

    Submission save(Submission submission);

    Submission saveSubmissionWithCodes(Submission submission, List<FileResource> originalCodes) throws IOException;

    String saveZippedSubmittedCodesAndGetFileId(StreamingResource streamingResource) throws IOException;

    Optional<FileResource> downloadZippedSubmittedCodes(String submissionId);

    Optional<SubmissionThrottling> findSubmissionThrottling(int problemId, int studentId);

    void saveSubmissionThrottling(SubmissionThrottling submissionThrottling);

    boolean submissionExists(String submissionId);

    List<Submission> findAllByIds(String... submissionIds);
}
