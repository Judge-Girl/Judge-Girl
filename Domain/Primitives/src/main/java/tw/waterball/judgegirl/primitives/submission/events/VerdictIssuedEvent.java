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

package tw.waterball.judgegirl.primitives.submission.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;

import java.util.Date;

import static java.util.stream.Collectors.joining;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@Getter
@EqualsAndHashCode(callSuper = true)
public class VerdictIssuedEvent extends Event {
    private final int problemId;
    private final String problemTitle;
    private final int studentId;
    private final String submissionId;
    private Verdict verdict;
    private final Date submissionTime;
    private final Bag submissionBag;

    public VerdictIssuedEvent(int problemId, String problemTitle, int studentId, String submissionId, Verdict verdict, Date submissionTime, Bag submissionBag) {
        super(VerdictIssuedEvent.class.getSimpleName());
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.studentId = studentId;
        this.submissionId = submissionId;
        this.verdict = verdict;
        this.submissionTime = submissionTime;
        this.submissionBag = submissionBag;
    }

    @Override
    public String toString() {
        return String.format("problemId=%d problemTitle=\"%s\" studentId=%d submissionId=%s submissionTime=%d " +
                        "summaryStatus=%s verdictIssuedTime=%d grade=%s, with bag: %s",
                problemId, problemTitle, studentId, submissionId, submissionTime.getTime(), verdict.getSummaryStatus(),
                verdict.getIssueTime().getTime(), getVerdict().getGrade(),
                submissionBag.entrySet().stream()
                        .map(entry -> String.format("%s=\"%s\"", entry.getKey(), entry.getValue()))
                        .collect(joining(" ")));

    }

    public void setVerdict(Verdict verdict) {
        this.verdict = verdict;
    }
}
