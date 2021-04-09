///*
// * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
// *   Licensed under the Apache License, Version 2.0 (the "License");
// *   you may not use this file except in compliance with the License.
// *   You may obtain a copy of the License at
// *       http://www.apache.org/licenses/LICENSE-2.0
// *   Unless required by applicable law or agreed to in writing, software
// *   distributed under the License is distributed on an "AS IS" BASIS,
// *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *   See the License for the specific language governing permissions and
// *   limitations under the License.
// */
//
//package tw.waterball.judgegirl.judger.tests;
//
//import org.apache.commons.io.IOUtils;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.ArgumentCaptor;
//import tw.waterball.judgegirl.commons.models.files.FileResource;
//import tw.waterball.judgegirl.entities.problem.*;
//import tw.waterball.judgegirl.entities.submission.report.Report;
//import tw.waterball.judgegirl.entities.submission.Submission;
//import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuer;
//import tw.waterball.judgegirl.judger.CCJudger;
//import tw.waterball.judgegirl.judger.DefaultCCJudgerFactory;
//import tw.waterball.judgegirl.plugins.api.AbstractJudgeGirlPlugin;
//import tw.waterball.judgegirl.plugins.api.JudgeGirlVerdictFilterPlugin;
//import tw.waterball.judgegirl.plugins.api.codeinspection.JudgeGirlSourceCodeFilterPlugin;
//import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
//import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
//import tw.waterball.judgegirl.problemapi.views.ProblemView;
//import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
//import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;
//import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
//import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuedEvent;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.*;
//
///**
// * TODO duplicate test's setup code with PrefixSumText
// *
// * @author - johnny850807@gmail.com (Waterball)
// */
//public class JudgeGirlFilterPluginTest {
//    private final static String zippedProvidedCodesFileName = "/judgeCases/prefixsum/provided.zip";
//    private final static String zippedTestcaseIOsFileName = "/judgeCases/prefixsum/io.zip";
//    private final static String zippedSubmittedCodesFileNameFormat = "/judgeCases/prefixsum/%s/submitted.zip";
//    private final static TestFilterPlugin filterPlugin = new TestFilterPlugin();
//    ;
//    private static int problemId = 1;
//    private static int studentId = 1;
//    private static String submissionId = "JudgeGirlFilterPluginTest";
//    private static Submission submission = new Submission(submissionId, studentId, problemId, "fileId");
//    private ProblemServiceDriver problemServiceDriver;
//    private SubmissionServiceDriver submissionServiceDriver;
//    private final static int MEMORY_LIMIT = 128 << 20;
//    private static ResourceSpec resourceSpec = new ResourceSpec(Language.C, ServerEnv.NORMAL, 2f, 0);
//    private static Problem problem = Problem.builder()
//            .id(problemId).title("Prefix Sum")
//            .description("Ignored")
//            .resourceSpec(resourceSpec)
//            .outputMatchPolicyPluginTag(AllMatchPolicyPlugin.TAG)
//            .tag("Ignored")
//            .filterPluginTag(filterPlugin.getTag())
//            .submittedCodeSpec(new SubmittedCodeSpec(Language.C, "prefixsum-seq.c"))
//            .providedCodesFileId("providedCodesFileId")
//            .testcaseIOsFileId("testcaseIOsFileId")
//            .compilation(new Compilation("gcc -std=c99 -O2 -pthread prefixsum-seq.c secret.c")).build();
//
//    private static List<Testcase> testCases = Arrays.asList(
//            new Testcase("1", problemId, 1000,
//                    MEMORY_LIMIT, MEMORY_LIMIT, -1, 30),
//            new Testcase("2", problemId, 1000,
//                    MEMORY_LIMIT, MEMORY_LIMIT, -1, 30),
//            new Testcase("3", problemId, 2000,
//                    MEMORY_LIMIT, MEMORY_LIMIT, -1, 40));
//    private VerdictPublisher verdictPublisher;
//    private CCJudger judger;
//
//    @BeforeEach
//    void setup() throws IOException {
//        problemServiceDriver = mock(ProblemServiceDriver.class);
//        submissionServiceDriver = mock(SubmissionServiceDriver.class);
//        verdictPublisher = mock(VerdictPublisher.class);
//
//        judger = DefaultCCJudgerFactory.create("/judger-layout.yaml",
//                problemServiceDriver, submissionServiceDriver, verdictPublisher, filterPlugin);
//    }
//
//
//    @Test
//    void GivenMyTestFilterPlugin_whenJudge_shouldInvokeFilter() throws IOException {
//        mockServiceDrivers();
//
//        judger.judge(studentId, problemId, submissionId);
//
//        assertTrue(filterPlugin.hasBeenInvokedSourceCodeFilter);
//        assertTrue(filterPlugin.hasBeenInvokedVerdictFilter);
//
//        VerdictIssuedEvent event = captureVerdictIssuedEvent();
//        event.getJudges().forEach(judge ->
//                assertEquals(JudgeStatus.AC, judge.getStatus(), "The status must have been filtered with the new status AC.")
//        );
//
//        var data = event.getReport().getRawData();
//        assertEquals(filterPlugin.report.getRawData(), data.get(filterPlugin.report.getName()));
//    }
//
//    private void mockServiceDrivers() throws IOException {
//        when(submissionServiceDriver.getSubmission(
//                problemId, studentId, submission.getId())).thenReturn(SubmissionView.fromEntity(submission));
//        when(problemServiceDriver.getProblem(problem.getId())).thenReturn(ProblemView.fromEntity(problem));
//        when(problemServiceDriver.getTestcases(problem.getId())).thenReturn(testCases);
//        mockDownloadRequests();
//    }
//
//    private void mockDownloadRequests() throws IOException {
//        String zippedSubmittedCodesFileName = String.format(zippedSubmittedCodesFileNameFormat, "AC");
//        byte[] zippedSubmittedCodesBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedSubmittedCodesFileName));
//        when(submissionServiceDriver.downloadSubmittedCodes(
//                problemId, studentId, submission.getId(), submission.getSubmittedCodesFileId()))
//                .thenReturn(new FileResource(zippedSubmittedCodesFileName,
//                        zippedSubmittedCodesBytes.length,
//                        new ByteArrayInputStream(zippedSubmittedCodesBytes)));
//
//        byte[] zippedProvidedCodeBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedProvidedCodesFileName));
//        when(problemServiceDriver.downloadProvidedCodes(problem.getId(), , problem.getProvidedCodesFileId()))
//                .thenReturn(new FileResource(zippedProvidedCodesFileName, zippedProvidedCodeBytes.length,
//                        new ByteArrayInputStream(zippedProvidedCodeBytes)));
//
//        byte[] zippedTestcaseInputsBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedTestcaseIOsFileName));
//        when(problemServiceDriver.downloadTestCaseIOs(problem.getId(), problem.getTestcaseIOsFileId()))
//                .thenReturn(new FileResource(zippedTestcaseIOsFileName,
//                        zippedTestcaseInputsBytes.length,
//                        new ByteArrayInputStream(zippedTestcaseInputsBytes)));
//
//    }
//
//    private VerdictIssuedEvent captureVerdictIssuedEvent() {
//        ArgumentCaptor<VerdictIssuedEvent> argumentCaptor = ArgumentCaptor.forClass(VerdictIssuedEvent.class);
//        verify(verdictPublisher).publish(argumentCaptor.capture());
//        return argumentCaptor.getValue();
//    }
//
//    public static class TestFilterPlugin extends AbstractJudgeGirlPlugin
//            implements JudgeGirlSourceCodeFilterPlugin, JudgeGirlVerdictFilterPlugin {
//        public boolean hasBeenInvokedSourceCodeFilter;
//        public boolean hasBeenInvokedVerdictFilter;
//        public Report report = new Report("TestReport",
//                Collections.singletonMap("my-data",
//                        Collections.singletonMap("Yo", "What's up")));
//
//        @Override
//        public void filter(Path sourceRootPath) {
//            hasBeenInvokedSourceCodeFilter = true;
//        }
//
//        @Override
//        public void filter(VerdictIssuer verdictIssuer) {
//            hasBeenInvokedVerdictFilter = true;
//            verdictIssuer.modifyJudges(j -> j.setStatus(JudgeStatus.AC))
//                    .addReport(report);
//        }
//
//        @Override
//        public String getDescription() {
//            return "SourceCodeFilteringPlugin for Testing";
//        }
//
//        @Override
//        public JudgePluginTag getTag() {
//            return new JudgePluginTag(JudgePluginTag.Type.FILTER, "test", "TestFilterPlugin", "1.0");
//        }
//    }
//}
