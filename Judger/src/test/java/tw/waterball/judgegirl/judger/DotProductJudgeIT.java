///*
// *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
// *
// *    Licensed under the Apache License, Version 2.0 (the "License");
// *    you may not use this file except in compliance with the License.
// *    You may obtain a copy of the License at
// *
// *        http://www.apache.org/licenses/LICENSE-2.0
// *
// *    Unless required by applicable law or agreed to in writing, software
// *    distributed under the License is distributed on an "AS IS" BASIS,
// *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *    See the License for the specific language governing permissions and
// *    limitations under the License.
// */
//
//package tw.waterball.judgegirl.judger;
//
//import org.apache.commons.io.IOUtils;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import tw.waterball.judgegirl.commons.models.files.FileResource;
//import tw.waterball.judgegirl.commons.services.mq.SubmissionMessageQueue;
//import tw.waterball.judgegirl.entities.submission.JudgeResponse;
//import tw.waterball.judgegirl.judger.beta.BashCompilerFactory;
//import tw.waterball.judgegirl.judger.beta.CCSandboxTestcaseExecutorFactory;
//import tw.waterball.judgegirl.plugins.api.PresetJudgeGirlPluginLocator;
//import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.*;
//
//// TODO the test should be parameterized in the future
//@SuppressWarnings("SameParameterValue")
//class DotProductJudgeIT {
//    private final static String zippedProvidedCodesFileName = "/problem/gpu/dotProduct/provided.zip";
//    private final static String zippedTestCaseInputsFileName = "/problem/gpu/dotProduct/in.zip";
//    private final static String zippedTestCaseOutputsFileName = "/problem/gpu/dotProduct/out.zip";
//    private final static String zippedSubmittedCodesFileName = "/problem/gpu/dotProduct/submitted.zip";
//    private final static String compilationScript = "gcc -std=c99 -O2 main.c -lOpenCL -fopenmp";
//    private final static long MEMORY_LIMIT = 512 * 1024 * 1024;
//    private static JudgeSpec judgeSpec = new JudgeSpec(JudgeLang.C, JudgeEnv.NORMAL, 0.5f, 1);
//    private static int problemId = 1;
//    private static Problem problem = Problem.builder()
//            .id(problemId).title("Dot Product")
//            .markdownDescription("Ignored")
//            .judgeSpec(judgeSpec)
//            .judgePolicyPluginTag(AllMatchPolicyPlugin.TAG)
//            .tag("Ignored")
//            .submittedCodeSpec(new SubmittedCodeSpec(JudgeLang.C, "main.c"))
//            .submittedCodeSpec(new SubmittedCodeSpec(JudgeLang.OPEN_CL, "vecdot.cl"))
//            .zippedProvidedCodesFileId("providedCodesFileId")
//            .zippedTestCaseInputsFileId("testcaseInputsFileId")
//            .zippedTestCaseOutputsFileId("testcaseOutputsFileId")
//            .compilation(new Compilation(compilationScript)).build();
//    private static List<TestCase> testCases = Arrays.asList(
//            new TestCase("1", problemId, 10000,
//                    MEMORY_LIMIT, MEMORY_LIMIT, 1024, 30),
//            new TestCase("2", problemId, 20000,
//                    MEMORY_LIMIT, MEMORY_LIMIT, 1024, 70));
//    private static int studentId = 1;
//    private static Submission submission = new Submission("DotProductJudgeIT", studentId, problem.getId(), "fileId");
//    private SubmissionServiceDriver submissionServiceDriver;
//    private ProblemServiceDriver problemServiceDriver;
//    private SubmissionMessageQueue submissionMessageQueue;
//    private CCJudger judger;
//    private String ADMIN_TOKEN = "admin";
//
//    @BeforeEach
//    void setup() {
//        submissionServiceDriver = mock(SubmissionServiceDriver.class);
//        problemServiceDriver = mock(ProblemServiceDriver.class);
//        submissionMessageQueue = mock(SubmissionMessageQueue.class);
//
//        judger = new CCJudger(ADMIN_TOKEN,
//                problemServiceDriver, submissionServiceDriver, submissionMessageQueue,
//                new BashCompilerFactory(), new CCSandboxTestcaseExecutorFactory(),
//                new PresetJudgeGirlPluginLocator(new AllMatchPolicyPlugin()), new JudgerWorkspace());
//    }
//
//
//    @Test
//    void judge_AC() throws IOException {
//        prepareServiceDriverStubs();
//
//        judger.doJudge(studentId, problemId, submission.getId());
//
//        JudgeResponse actualJudgeResponse = verifyPublishJudgeResultToMessageQueueAndGetJudge();
//        verifyJudgeResponse(actualJudgeResponse);
//    }
//
//
//    private void prepareServiceDriverStubs() throws IOException {
//        when(submissionServiceDriver.getSubmission(ADMIN_TOKEN,
//                problemId, studentId, submission.getId())).thenReturn(submission);
//        when(problemServiceDriver.getProblem(problem.getId())).thenReturn(problem);
//        when(problemServiceDriver.getTestCases(problem.getId())).thenReturn(testCases);
//
//        prepareAllZippedFiles();
//    }
//
//
//    private void prepareAllZippedFiles() throws IOException {
//        byte[] zippedSubmittedCodesBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedSubmittedCodesFileName));
//        when(submissionServiceDriver.getZippedSubmittedCodes(ADMIN_TOKEN,
//                problemId, studentId, submission.getId())).thenReturn(
//                new FileResource(zippedSubmittedCodesFileName, zippedSubmittedCodesBytes.length,
//                        new ByteArrayInputStream(zippedSubmittedCodesBytes)));
//
//
//        byte[] zippedProvidedCodeBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedProvidedCodesFileName));
//        when(problemServiceDriver.getZippedProvidedCodes(problem.getId())).thenReturn(
//                new FileResource(zippedProvidedCodesFileName, zippedProvidedCodeBytes.length,
//                        new ByteArrayInputStream(zippedProvidedCodeBytes)));
//
//
//        byte[] zippedTestCaseInputsBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedTestCaseInputsFileName));
//        when(problemServiceDriver.getZippedTestCaseInputs(problem.getId())).thenReturn(
//                new FileResource(zippedTestCaseInputsFileName, zippedTestCaseInputsBytes.length,
//                        new ByteArrayInputStream(zippedTestCaseInputsBytes)));
//
//        byte[] zippedTestCaseOutputsBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedTestCaseOutputsFileName));
//        when(problemServiceDriver.getZippedTestCaseOutputs(problem.getId())).thenReturn(
//                new FileResource(zippedTestCaseOutputsFileName, zippedTestCaseOutputsBytes.length,
//                        new ByteArrayInputStream(zippedTestCaseOutputsBytes)));
//
//    }
//
//
//    private JudgeResponse verifyPublishJudgeResultToMessageQueueAndGetJudge() {
//        ArgumentCaptor<JudgeResponse> argumentCaptor = ArgumentCaptor.forClass(JudgeResponse.class);
//        verify(submissionMessageQueue).publish(argumentCaptor.capture());
//        return argumentCaptor.getValue();
//    }
//
//    private void verifyJudgeResponse(JudgeResponse judgeResponse) {
//        for (int i = 0; i < testCases.size(); i++) {
//            Judge judge = judgeResponse.get(i);
//            TestCase testCase = testCases.get(i);
//            assertEquals(JudgeStatus.AC, judge.getStatus());
//            assertEquals(testCase.getGrade(), judge.getGrade());
//            assertEquals(testCase.getName(), judge.getTestCaseName());
//            assertEquals("", judge.getErrorMessage(), "Error message should be empty if AC");
//            assertTrue(judge.getRuntime() <= testCase.getTimeLimit());
//            assertTrue(judge.getMemory() <= testCase.getMemoryLimit());
//        }
//        assertEquals(problemId, judgeResponse.getProblemId());
//        assertEquals(problem.getTitle(), judgeResponse.getProblemTitle());
//        assertEquals(submission.getId(), judgeResponse.getSubmissionId());
//        assertEquals("", judgeResponse.getCompileErrorMessage(), "Compile error message should be empty if succeed.");
//
//    }
//
//
//}
