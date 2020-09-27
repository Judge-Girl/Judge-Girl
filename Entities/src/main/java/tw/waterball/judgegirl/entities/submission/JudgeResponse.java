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

import java.util.LinkedList;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class JudgeResponse {
    private int problemId;
    private String problemTitle;

    private String submissionId;
    private String compileErrorMessage = "";

    @Singular
    private List<Judge> judges = new LinkedList<>();

    public JudgeResponse(int problemId, String problemTitle, String submissionId) {
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.submissionId = submissionId;
        this.compileErrorMessage = "";
    }

    public Judge get(int index) {
        return judges.get(index);
    }

    public void addJudge(Judge judge) {
        judges.add(judge);
    }

    public void removeJudge(Judge judge) {
        judges.remove(judge);
    }
}
