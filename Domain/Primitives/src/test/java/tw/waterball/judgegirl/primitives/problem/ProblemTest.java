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

package tw.waterball.judgegirl.primitives.problem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.waterball.judgegirl.commons.utils.StringUtils.generateStringOfLength;
import static tw.waterball.judgegirl.primitives.stubs.ProblemStubs.problemTemplate;

class ProblemTest {

    @Test
    void GivenValidProblem_WhenValidate_ShouldPass() {
        problemTemplate().build();
    }

    @Test
    void GivenProblemWithMatchPolicyPluginTagOfSourceCodeFilterType_WhenValidate_ShouldThrow() {
        assertThrows(RuntimeException.class,
                () -> problemTemplate()
                        .outputMatchPolicyPluginTag(new JudgePluginTag(
                                JudgePluginTag.Type.FILTER,
                                "group", "name", "1.0"
                        )).build());
    }


    @Test
    void GivenProblemWithCodeInspectionOfOutputMatchType_WhenValidate_ShouldThrow() {
        assertThrows(RuntimeException.class,
                () -> problemTemplate()
                        .filterPluginTag(new JudgePluginTag(
                                JudgePluginTag.Type.OUTPUT_MATCH_POLICY,
                                "group", "name", "1.0"
                        )).build());
    }

    @Test
    void LongTitleWithLength100ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> problemTemplate()
                        .title(generateStringOfLength('x', 100)).build());
    }

    @Test
    void EmptyTitleShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> problemTemplate().title("").build());
    }

    @Test
    void LongDescriptionWithLength3001ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> problemTemplate()
                        .description(generateStringOfLength('x', 3001)).build());
    }

}