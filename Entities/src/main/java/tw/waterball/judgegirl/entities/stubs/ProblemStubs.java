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

package tw.waterball.judgegirl.entities.stubs;

import tw.waterball.judgegirl.entities.problem.*;
import tw.waterball.judgegirl.entities.problem.Problem.ProblemBuilder;
import tw.waterball.judgegirl.entities.submission.CompositeReport;
import tw.waterball.judgegirl.entities.submission.Report;

import static java.util.Collections.singletonMap;
import static tw.waterball.judgegirl.entities.problem.JudgePluginTag.Type.OUTPUT_MATCH_POLICY;
import static tw.waterball.judgegirl.entities.problem.Language.C;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ProblemStubs {
    @SuppressWarnings("WeakerAccess")
    public final static int ID = 1;

    public static ProblemBuilder template() {
        return Problem.builder()
                .id(ID)
                .title("Title")
                .description("# Title \n ```\n Code \n ```")
                .outputMatchPolicyPluginTag(new JudgePluginTag(
                        OUTPUT_MATCH_POLICY, "group", "name", "1.0"))
                .tag("tag1").tag("tag2")
                .testcaseIOsFileId("testcaseIOsFileId")
                .languageEnv(C.toString(),
                        LanguageEnv.builder()
                                .language(C)
                                .compilation(new Compilation("Compilation Script"))
                                .resourceSpec(new ResourceSpec(0.5f, 0))
                                .submittedCodeSpec(new SubmittedCodeSpec(C, "main.c"))
                                .providedCodesFileId("providedCodesFileId")
                                .build())
                .testcase(new Testcase("1", ID, 5, 5, 5000, 1, 20))
                .testcase(new Testcase("2", ID, 5, 5, 5000, 1, 30))
                .testcase(new Testcase("3", ID, 3, 4, 5000, 1, 50));
    }

    public static CompositeReport compositeReport() {
        CompositeReport compositeReport = new CompositeReport();
        compositeReport.addReport(new Report("A"));
        compositeReport.addReport(new Report("B"));
        compositeReport.addReport(new Report("C", () -> singletonMap("C-1", 1)));
        return compositeReport;
    }
}
