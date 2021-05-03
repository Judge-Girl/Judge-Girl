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

package tw.waterball.judgegirl.entities.problem;

import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.entities.stubs.ProblemStubs;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ProblemTest {

    @Test
    void GivenValidProblem_WhenValidate_ShouldPass() {
        Problem problem = ProblemStubs.problemTemplate().build();
        problem.validate();
    }

    @Test
    void GivenProblemWithMatchPolicyPluginTagOfSourceCodeFilterType_WhenValidate_ShouldThrow() {
        Problem problem = ProblemStubs.problemTemplate()
                .outputMatchPolicyPluginTag(new JudgePluginTag(
                        JudgePluginTag.Type.FILTER,
                        "group", "name", "1.0"
                )).build();

        assertThrows(RuntimeException.class, problem::validate);
    }


    @Test
    void GivenProblemWithCodeInspectionOfOutputMatchType_WhenValidate_ShouldThrow() {
        Problem problem = ProblemStubs.problemTemplate()
                .filterPluginTag(new JudgePluginTag(
                        JudgePluginTag.Type.OUTPUT_MATCH_POLICY,
                        "group", "name", "1.0"
                )).build();

        assertThrows(RuntimeException.class, problem::validate);
    }
}