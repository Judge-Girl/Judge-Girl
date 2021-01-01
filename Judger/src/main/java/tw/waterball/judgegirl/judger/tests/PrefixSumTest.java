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

package tw.waterball.judgegirl.judger.tests;

import tw.waterball.judgegirl.entities.problem.*;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;

import java.util.Arrays;
import java.util.List;

public class PrefixSumTest extends AbstractJudgerTest {
    @Override
    protected Problem getProblem(Problem.ProblemBuilder problemBuilder) {
        return problemBuilder.id(1).title("prefixsum")
                .judgeEnvSpec(new JudgeEnvSpec(Language.C, JudgeEnv.NORMAL, 2f, 0))
                .outputMatchPolicyPluginTag(AllMatchPolicyPlugin.TAG)
                .submittedCodeSpec(new SubmittedCodeSpec(Language.C, "prefixsum-seq.c"))
                .compilation(new Compilation("gcc -std=c99 -O2 -pthread prefixsum-seq.c secret.c"))
                .build();
    }

    @Override
    protected List<Testcase> getTestCases() {
        final int MEMORY_LIMIT = 120 << 20;
        return Arrays.asList(
                new Testcase("1", 1, 1000,
                        MEMORY_LIMIT, MEMORY_LIMIT, -1, 30),
                new Testcase("2", 1, 1000,
                        MEMORY_LIMIT, MEMORY_LIMIT, -1, 30),
                new Testcase("3", 1, 2500,
                        MEMORY_LIMIT, MEMORY_LIMIT, -1, 40));
    }
}
