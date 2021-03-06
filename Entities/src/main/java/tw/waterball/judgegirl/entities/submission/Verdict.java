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

import lombok.Singular;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.entities.exceptions.InvalidVerdictException;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Verdict {
    @Singular
    private List<Judge> judges;
    @Nullable
    private String compileErrorMessage;
    private Date issueTime;
    private Report report = Report.EMPTY;


    public Verdict(List<Judge> judges) throws InvalidVerdictException {
        this(judges, new Date());
    }

    public Verdict(List<Judge> judges, Date issueTime) throws InvalidVerdictException {
        this.judges = judges;
        this.issueTime = issueTime;
        if (getSummaryStatus() == JudgeStatus.NONE) {
            throw new InvalidVerdictException("Verdict must not have a NONE summary status.");
        }
    }

    public Verdict(String compileErrorMessage) throws InvalidVerdictException {
        this(compileErrorMessage, new Date());
    }

    public Verdict(String compileErrorMessage, Date issueTime) throws InvalidVerdictException {
        this.issueTime = issueTime;
        this.judges = Collections.emptyList();
        setCompileErrorMessage(compileErrorMessage);
    }

    public static Verdict compileError(String compileErrorMessage) {
        return new Verdict(compileErrorMessage, new Date());
    }

    public static Verdict compileError(String compileErrorMessage, Date issueTime) {
        return new Verdict(compileErrorMessage, issueTime);
    }

    @Deprecated
    public static Verdict compileError(String compileErrorMessage, List<Judge> judges) {
        Verdict verdict = new Verdict(judges);
        verdict.setCompileErrorMessage(compileErrorMessage);
        return verdict;
    }

    public Integer getTotalGrade() {
        if (judges.isEmpty()) {
            return 0;
        }
        return judges.stream()
                .mapToInt(Judge::getGrade).sum();
    }

    public JudgeStatus getSummaryStatus() {
        if (compileErrorMessage != null && !compileErrorMessage.isEmpty()) {
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
                .orElse(JudgeStatus.NONE);
    }

    public long getMaximumRuntime() {
        return judges.stream()
                .mapToLong(j -> j.getProgramProfile().getRuntime())
                .max().orElseThrow(() -> new IllegalStateException("A verdict that doesn't have judges."));
    }

    public long getMaximumMemoryUsage() {
        return judges.stream()
                .mapToLong(j -> j.getProgramProfile().getMemoryUsage())
                .max().orElseThrow(() -> new IllegalStateException("A verdict that doesn't have judges."));
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
        return compileErrorMessage != null;
    }

    @Nullable
    public String getCompileErrorMessage() {
        return compileErrorMessage;
    }

    public void setCompileErrorMessage(@Nullable String compileErrorMessage) {
        this.compileErrorMessage = compileErrorMessage;
        validateCompileErrorStatusConsistency();
    }

    private void validateCompileErrorStatusConsistency() {
        if (isCompileError()) {
            boolean allJudgesAreCE = judges.stream()
                    .allMatch(judge -> judge.getStatus() == JudgeStatus.CE);
            if (!allJudgesAreCE) {
                throw new IllegalStateException("Inconsistent status," +
                        " all the judges in a compile error verdict should also have a status of CE.");
            }
        }
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

}
