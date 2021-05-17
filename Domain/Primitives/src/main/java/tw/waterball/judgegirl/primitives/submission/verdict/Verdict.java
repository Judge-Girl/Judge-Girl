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

package tw.waterball.judgegirl.primitives.submission.verdict;

import lombok.Singular;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.primitives.grading.Grade;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.report.CompositeReport;
import tw.waterball.judgegirl.primitives.submission.report.Report;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StringUtils.isNullOrEmpty;
import static tw.waterball.judgegirl.primitives.time.DateProvider.now;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Verdict implements Comparable<Verdict> {
    @Singular
    private final List<Judge> judges;
    @Nullable
    private String compileErrorMessage;
    private Date issueTime;
    private Report report = Report.EMPTY;

    private final Grade grade;

    public Verdict(List<Judge> judges) throws InvalidVerdictException {
        this(judges, now());
    }

    // Summary Status: CE, the maxGrade is sourced here.
    public Verdict(String compileErrorMessage, int maxGrade, Date issueTime) throws InvalidVerdictException {
        this.issueTime = issueTime;
        this.judges = Collections.emptyList();
        this.grade = new Grade(0, maxGrade);
        setCompileErrorMessage(compileErrorMessage);
    }

    public Verdict(List<Judge> judges, Date issueTime) throws InvalidVerdictException {
        this.judges = judges;
        this.issueTime = issueTime;
        this.grade = new Grade(judges);
        mustHaveAtLeastOneJudge();
        mustNotHaveNoneJudgeStatus();
    }

    private void mustHaveAtLeastOneJudge() {
        if (judges.isEmpty()) {
            throw new InvalidVerdictException("Verdict must have at least one judge.");
        }
    }

    private void mustNotHaveNoneJudgeStatus() {
        if (judges.stream().map(Judge::getStatus)
                .anyMatch(status -> status == JudgeStatus.NONE)) {
            throw new InvalidVerdictException("Verdict must not have any NONE judge status in any of its judges.");
        }
    }

    public static Verdict compileError(String compileErrorMessage, int maxGrade) {
        return new Verdict(compileErrorMessage.trim(), maxGrade, now());
    }

    public static Verdict compileError(String compileErrorMessage, int maxGrade, Date issueTime) {
        return new Verdict(compileErrorMessage.trim(), maxGrade, issueTime);
    }

    public int getMaxGrade() {
        return grade.max();
    }

    public int getGrade() {
        return grade.value();
    }

    public JudgeStatus getSummaryStatus() {
        if (isCompileError()) {
            return JudgeStatus.CE;
        }
        return judges.stream().map(Judge::getStatus)
                .min((s1, s2) -> {
                    if (s1 == JudgeStatus.AC) {
                        // puts AC at the tail, as if the submission is incorrect in certain test cases,
                        // the summary should indicate 'Error' regardless how many ACs he got.
                        return 1;
                    }
                    return s1 == s2 ? 0 : -1;
                })
                .orElseThrow(() -> new InvalidVerdictException("Verdict doesn't have judges or have NONE judge status."));
    }

    public long getMaximumRuntime() {
        if (isCompileError()) {
            return 0;
        }
        return judges.stream()
                .mapToLong(j -> j.getProgramProfile().getRuntime())
                .max().orElseThrow(() -> new IllegalStateException("A verdict that doesn't have judges."));
    }

    public long getMaximumMemoryUsage() {
        if (isCompileError()) {
            return 0;
        }
        return judges.stream()
                .mapToLong(j -> j.getProgramProfile().getMemoryUsage())
                .max().orElseThrow(() -> new IllegalStateException("A verdict that doesn't have judges."));
    }

    public Judge getWorseJudge() {
        if (isCompileError()) {
            return null;
        }
        return Collections.min(judges);
    }

    public Judge getBestJudge() {
        if (isCompileError()) {
            return null;
        }
        return Collections.max(judges);
    }

    public List<Judge> getJudges() {
        return judges;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public boolean isCompileError() {
        return !isNullOrEmpty(compileErrorMessage);
    }

    @Nullable
    public String getCompileErrorMessage() {
        return compileErrorMessage;
    }

    public void setCompileErrorMessage(@Nullable String compileErrorMessage) {
        this.compileErrorMessage = compileErrorMessage;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public void addReport(Report report) {
        if (this.report == Report.EMPTY) {
            this.report = new CompositeReport();
        }
        ((CompositeReport) this.report).addReport(report);
    }

    @Override
    public int compareTo(@NotNull Verdict verdict) {
        int myGrade = getGrade();
        int hisGrade = verdict.getGrade();
        if (this.isCompileError() && verdict.isCompileError()) {
            return 0;
        }
        if (this.isCompileError()) {
            return -1;
        }
        if (verdict.isCompileError()) {
            return 1;
        }
        if (myGrade == hisGrade) {
            return getBestJudge().compareTo(verdict.getBestJudge());
        }
        return myGrade - hisGrade;
    }
}
