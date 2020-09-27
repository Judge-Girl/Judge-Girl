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

import lombok.*;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString

// TODO refactoring ignore unknown properties for 'summaryStatus'
// @JsonIgnoreProperties(ignoreUnknown = true)

public class Submission {
    private String id;
    private int studentId;
    private int problemId;

    @Singular
    private List<Judge> judges = new ArrayList<>();

    private Verdict verdict;

    private String zippedSubmittedCodeFilesId;
    private String compileErrorMessage = "";

    private Date submissionTime = new Date();
    private Date judgeTime;

    public Submission(String id, int studentId, int problemId, String zippedSubmittedCodeFilesId) {
        this.id = id;
        this.studentId = studentId;
        this.problemId = problemId;
        this.zippedSubmittedCodeFilesId = zippedSubmittedCodeFilesId;
    }

    public Submission(int studentId, int problemId, String zippedSubmittedCodeFilesId) {
        this.studentId = studentId;
        this.problemId = problemId;
        this.zippedSubmittedCodeFilesId = zippedSubmittedCodeFilesId;
    }

    public Integer getTotalGrade() {
        if (judges.isEmpty()) {
            return null;
        }
        int sum = 0;
        for (Judge judge : judges) {
            sum += judge.getGrade();
        }
        return sum;
    }

    public JudgeStatus getSummaryStatus() {
        return judges.stream().map(Judge::getStatus)
                .min((s1, s2) -> {
                    if (s1 == JudgeStatus.AC) {
                        // puts AC at the tail, as if the submission is incorrect in certain test cases,
                        // the summary should indicate 'Error' regardless how many ACs he got.
                        return 1;
                    }
                    return s1 == s2 ? 0 : -1;
                })
                .orElse(JudgeStatus.NONE);

    }

    // TODO @JsonIgnore
    public boolean isJudged() {
        return judges != null && judges.size() > 0 && judgeTime != null;
    }
}
