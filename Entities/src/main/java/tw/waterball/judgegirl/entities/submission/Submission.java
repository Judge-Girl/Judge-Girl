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

package tw.waterball.judgegirl.entities.submission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Submission implements Comparable<Submission> {
    private String id;
    private int problemId;
    private int studentId;
    private String languageEnvName;

    @Nullable
    private Verdict verdict;

    private String submittedCodesFileId;
    private Date submissionTime = new Date();

    public Submission(String id, int studentId, int problemId, String languageEnvName, String submittedCodesFileId, Date submissionTime) {
        this(id, studentId, problemId, languageEnvName, submittedCodesFileId);
        this.submissionTime = submissionTime;
    }

    public Submission(int studentId, int problemId, String languageEnvName) {
        this(studentId, problemId, languageEnvName, null);
    }

    public Submission(int studentId, int problemId, String languageEnvName, String submittedCodesFileId) {
        this(null, studentId, problemId, languageEnvName, submittedCodesFileId);
    }

    public Submission(String id, int studentId, int problemId, String languageEnvName, String submittedCodesFileId) {
        this.id = id;
        this.studentId = studentId;
        this.problemId = problemId;
        this.languageEnvName = languageEnvName;
        this.submittedCodesFileId = submittedCodesFileId;
    }

    public boolean isJudged() {
        return getVerdict().isPresent();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getProblemId() {
        return problemId;
    }

    public Optional<Verdict> getVerdict() {
        return Optional.ofNullable(verdict);
    }

    public void setVerdict(@Nullable Verdict verdict) {
        this.verdict = verdict;
    }

    public String getSubmittedCodesFileId() {
        return submittedCodesFileId;
    }

    public void setSubmittedCodesFileId(String submittedCodesFileId) {
        this.submittedCodesFileId = submittedCodesFileId;
    }

    public Date getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Date submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Date getJudgeTime() {
        return getVerdict().map(Verdict::getIssueTime).orElse(null);
    }

    public String getLanguageEnvName() {
        return languageEnvName;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }

    @Override
    public int compareTo(@NotNull Submission submission) {
        if (!isJudged()) {
            if (!submission.isJudged()) {
                return 0;
            }
            return -1;
        }
        if (!submission.isJudged()) {
            return 1;
        }
        return getVerdict().orElseThrow()
                .compareTo(submission.getVerdict().orElseThrow());
    }
}
