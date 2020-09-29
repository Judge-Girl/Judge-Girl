/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.springboot.problem.controllers;

import tw.waterball.judgegirl.entities.problem.*;
import tw.waterball.judgegirl.entities.problem.Problem.ProblemBuilder;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Stubs {

    public final static int STUB_ID = 1;

    public final static ProblemBuilder PROBLEM_TEMPLATE_BUILDER = Problem.builder()
            .id(STUB_ID)
            .title("Title")
            .markdownDescription("# Title \n ```\n Code \n ```")
            .judgeSpec(new JudgeSpec(JudgeLang.C, JudgeEnv.NORMAL, 0.5f, 0))
            .judgePolicyPluginTag(new JudgePluginTag("type", "group", "name", "1.0"))
            .tag("tag1").tag("tag2")
            .submittedCodeSpec(new SubmittedCodeSpec(JudgeLang.C, "main.c"))
            .zippedProvidedCodesFileId("providedCodesFileId")
            .zippedTestCaseInputsFileId("testcaseInputsFileId")
            .zippedTestCaseOutputsFileId("testcaseOutputsFileId")
            .compilation(new Compilation("compilation script"))
            .inputFileName("file.in")
            .outputFileName("file.out");


}