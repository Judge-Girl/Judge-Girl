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

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Stubs {

    @SuppressWarnings("WeakerAccess")
    public final static int STUB_ID = 1;

    public final static ProblemBuilder problemTemplateBuilder() {
        return Problem.builder()
                .id(STUB_ID)
                .title("Title")
                .markdownDescription("# Template Description")
                .judgeEnvSpec(new JudgeEnvSpec(Language.C, JudgeEnv.NORMAL, 0.5f, 0))
                .outputMatchPolicyPluginTag(new JudgePluginTag(
                        JudgePluginTag.Type.OUTPUT_MATCH_POLICY, "group", "name", "1.0"))
                .tag("tag1").tag("tag2")
                .submittedCodeSpec(new SubmittedCodeSpec(Language.C, "main.c"))
                .providedCodesFileId("providedCodesFileId")
                .testcaseIOsFileId("testcaseIOsFileId")
                .compilation(new Compilation("compilation script"))
                .inputFileName("file.in")
                .outputFileName("file.out");
    }

    public final static CompositeReport compositeReport() {
        CompositeReport compositeReport = new CompositeReport();
        compositeReport.addReport(new Report("A"));
        compositeReport.addReport(new Report("B"));
        compositeReport.addReport(new Report("C", () -> singletonMap("C-1", 1)));
        return compositeReport;
    }
}
