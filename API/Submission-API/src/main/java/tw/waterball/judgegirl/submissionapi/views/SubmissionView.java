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

package tw.waterball.judgegirl.submissionapi.views;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;

import java.util.Date;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@NoArgsConstructor
public class SubmissionView {
    public String id;
    public int studentId;
    public int problemId;
    public String languageEnvName;
    public VerdictView verdict;
    public String submittedCodesFileId;
    public Date submissionTime;
    public Map<String, String> bag;
    public boolean judged;

    public SubmissionView(String id, int studentId, int problemId, String languageEnvName,
                          @Nullable VerdictView verdict, String submittedCodesFileId, Date submissionTime) {
        this(id, studentId, problemId, languageEnvName, verdict, submittedCodesFileId, submissionTime,
                emptyMap());
    }

    public SubmissionView(String id, int studentId, int problemId, String languageEnvName,
                          @Nullable VerdictView verdict, String submittedCodesFileId, Date submissionTime,
                          Map<String, String> bag) {
        this.id = id;
        this.studentId = studentId;
        this.problemId = problemId;
        this.languageEnvName = languageEnvName;
        this.verdict = verdict;
        this.judged = verdict != null;
        this.submittedCodesFileId = submittedCodesFileId;
        this.submissionTime = submissionTime;
        this.bag = bag;
    }

    public static SubmissionView toViewModel(@Nullable Submission submission) {
        if (submission == null) {
            return null;
        }
        Verdict verdict = submission.mayHaveVerdict().orElse(null);
        return new SubmissionView(submission.getId(),
                submission.getStudentId(),
                submission.getProblemId(),
                submission.getLanguageEnvName(),
                VerdictView.toViewModel(verdict),
                submission.getSubmittedCodesFileId(),
                submission.getSubmissionTime(),
                submission.getBag());
    }

    public static Submission toEntity(@Nullable SubmissionView submissionView) {
        if (submissionView == null) {
            return null;
        }

        VerdictView verdictView = submissionView.getVerdict();
        Submission submission = new Submission(submissionView.getId(),
                submissionView.getStudentId(),
                submissionView.getProblemId(),
                submissionView.getLanguageEnvName(),
                submissionView.getSubmittedCodesFileId(),
                submissionView.submissionTime);
        submission.setBag(new Bag(submissionView.getBag()));

        Verdict verdict = VerdictView.toEntity(verdictView);
        submission.setVerdict(verdict);
        return submission;
    }
}
