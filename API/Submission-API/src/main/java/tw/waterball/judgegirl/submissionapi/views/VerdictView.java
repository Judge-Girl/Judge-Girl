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

import lombok.*;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.verdict.Judge;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;

import java.util.Date;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerdictView {
    @Singular
    private List<Judge> judges;
    private JudgeStatus summaryStatus;
    private int totalGrade;
    private String compileErrorMessage;
    private long maximumRuntime;
    private long maximumMemoryUsage;
    private ReportView report;
    private Date issueTime;

    public static VerdictView toViewModel(@Nullable Verdict verdict) {
        if (verdict == null) {
            return null;
        }
        return new VerdictView(verdict.getJudges(),
                verdict.getSummaryStatus(),
                verdict.getTotalGrade(),
                verdict.getCompileErrorMessage(),
                verdict.getMaximumRuntime(),
                verdict.getMaximumMemoryUsage(),
                ReportView.toViewModel(verdict.getReport()),
                verdict.getIssueTime());
    }

    public static Verdict toEntity(@Nullable VerdictView verdictView) {
        if (verdictView == null) {
            return null;
        }
        Verdict verdict = new Verdict(
                verdictView.getJudges(),
                verdictView.getIssueTime());
        verdict.setReport(verdictView.getReport().toEntity());
        return verdict;
    }
}
