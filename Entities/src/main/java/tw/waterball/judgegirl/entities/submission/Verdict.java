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

import java.util.Date;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Verdict {
    @Singular
    private List<Judge> judges;
    private @Nullable String compileErrorMessage;
    private Date issueTime = new Date();

    public Verdict(List<Judge> judges, Date issueTime) throws InvalidVerdictException {
        this(judges);
        this.issueTime = issueTime;
    }

    public Verdict(List<Judge> judges) throws InvalidVerdictException {
        this.judges = judges;
        if (getSummaryStatus() == JudgeStatus.NONE) {
            throw new InvalidVerdictException("Verdict must not have a NONE summary status.");
        }
    }

    public void setCompileErrorMessage(@Nullable String compileErrorMessage) {
        this.compileErrorMessage = compileErrorMessage;
    }

    public boolean isCompileError() {
        return compileErrorMessage != null;
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

    public List<Judge> getJudges() {
        return judges;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public String getCompileErrorMessage() {
        return compileErrorMessage;
    }
}
