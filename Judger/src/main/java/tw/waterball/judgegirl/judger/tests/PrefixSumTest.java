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

import java.util.List;

import static java.util.Arrays.asList;

@SuppressWarnings("SameParameterValue")
public class PrefixSumTest extends AbstractJudgerTest {
    private static final int problemId = 9999999;
    private final static int MEMORY_LIMIT = 128 << 20;
    public static final String COMPILATION_SCRIPT = "gcc -std=c99 -O2 -pthread prefixsum-seq.c secret.c";
    private static final List<Testcase> testcases = asList(
            new Testcase("1", problemId, 1000,
                    MEMORY_LIMIT, MEMORY_LIMIT, -1, 30),
            new Testcase("2", problemId, 1000,
                    MEMORY_LIMIT, MEMORY_LIMIT, -1, 30),
            new Testcase("3", problemId, 2500,
                    MEMORY_LIMIT, MEMORY_LIMIT, -1, 40));

    private static final LanguageEnv languageEnv = LanguageEnv.builder()
            .language(Language.C)
            .resourceSpec(new ResourceSpec(2f, 0))
            .submittedCodeSpec(new SubmittedCodeSpec(Language.C, "prefixsum-seq.c"))
            .providedCodesFileId("providedCodesFileId")
            .compilation(new Compilation(COMPILATION_SCRIPT)).build();


    @Override
    protected Problem getProblem() {
        return Problem.builder()
                .id(problemId).title("Prefix-Sum")
                .description("Description")
                .languageEnv(languageEnv.getName(), languageEnv)
                .outputMatchPolicyPluginTag(AllMatchPolicyPlugin.TAG)
                .testcases(testcases)
                .build();
    }


}
