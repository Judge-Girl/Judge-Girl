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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.entities.submission.Judge;
import tw.waterball.judgegirl.entities.submission.Verdict;

import java.util.Date;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerdictView {
    private List<Judge> judges;
    private Date issueTime;

    public static VerdictView fromEntity(@Nullable Verdict verdict) {
        if (verdict == null) {
            return null;
        }
        return new VerdictView(verdict.getJudges(), verdict.getIssueTime());
    }

    public static Verdict toEntity(@Nullable VerdictView verdictView) {
        if (verdictView == null) {
            return null;
        }
        return new Verdict(
                verdictView.getJudges(),
                verdictView.getIssueTime());
    }
}
