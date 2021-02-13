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

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.problem.*;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.judger.CCJudger;
import tw.waterball.judgegirl.judger.DefaultCCJudgerFactory;
import tw.waterball.judgegirl.plugins.impl.cqi.CodeQualityInspectionPlugin;
import tw.waterball.judgegirl.plugins.impl.cqi.CodeQualityInspectionReport;
import tw.waterball.judgegirl.plugins.impl.cqi.CodingStyleAnalyzeReport;
import tw.waterball.judgegirl.plugins.impl.cqi.CyclomaticComplexityReport;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionapi.views.VerdictIssuedEvent;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * TODO duplicate test's setup code with PrefixSumText
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class CodeQualityInspectionPluginTest {
    private final static String zippedProvidedCodesFileName = "/judgeCases/prefixsum/provided.zip";
    private final static String zippedTestcaseIOsFileName = "/judgeCases/prefixsum/io.zip";
    private final static String zippedSubmittedCodesFileNameFormat = "/judgeCases/prefixsum/%s/submitted.zip";

    private static int problemId = 1;
    private static int studentId = 1;
    private static String submissionId = "CodeQualityInspectionPluginTest";
    private static Submission submission = new Submission(submissionId, studentId, problemId, "fileId");
    private ProblemServiceDriver problemServiceDriver;
    private SubmissionServiceDriver submissionServiceDriver;
    private final static int MEMORY_LIMIT = 128 << 20;
    private static ResourceSpec resourceSpec = new ResourceSpec(Language.C, ServerEnv.NORMAL, 2f, 0);
    private static Problem problem = Problem.builder()
            .id(problemId).title("Prefix Sum")
            .description("Ignored")
            .resourceSpec(resourceSpec)
            .outputMatchPolicyPluginTag(AllMatchPolicyPlugin.TAG)
            .tag("Ignored")
            .filterPluginTag(CodeQualityInspectionPlugin.TAG)
            .submittedCodeSpec(new SubmittedCodeSpec(Language.C, "prefixsum-seq.c"))
            .providedCodesFileId("providedCodesFileId")
            .testcaseIOsFileId("testcaseIOsFileId")
            .compilation(new Compilation("gcc -std=c99 -O2 -pthread prefixsum-seq.c secret.c")).build();

    private static List<Testcase> testCases = Arrays.asList(
            new Testcase("1", problemId, 1000,
                    MEMORY_LIMIT, MEMORY_LIMIT, -1, 30),
            new Testcase("2", problemId, 1000,
                    MEMORY_LIMIT, MEMORY_LIMIT, -1, 30),
            new Testcase("3", problemId, 2000,
                    MEMORY_LIMIT, MEMORY_LIMIT, -1, 40));
    private VerdictPublisher verdictPublisher;
    private CCJudger judger;

    @BeforeEach
    void setup() throws IOException {
        problemServiceDriver = mock(ProblemServiceDriver.class);
        submissionServiceDriver = mock(SubmissionServiceDriver.class);
        verdictPublisher = mock(VerdictPublisher.class);

        judger = DefaultCCJudgerFactory.create("/judger-layout.yaml",
                problemServiceDriver, submissionServiceDriver, verdictPublisher);
    }


    @SuppressWarnings("unchecked")
    @Test
    void testCodeQualityInspectionPlugin() throws IOException {
        mockServiceDrivers();

        judger.judge(studentId, problemId, submissionId);

        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        Map<String, ?> data = event.getReport().getRawData();

        assertTrue(data.containsKey(CodeQualityInspectionReport.NAME));
        Map<String, ?> cqiReportData = (Map<String, ?>) data.get(CodeQualityInspectionReport.NAME);

        assertTrue(cqiReportData.containsKey(CyclomaticComplexityReport.NAME));

        Map<String, ?> ccReportData = (Map<String, ?>) cqiReportData.get(CyclomaticComplexityReport.NAME);
        Object ccScore = ccReportData.get("ccScore");
        assertTrue(((int) ccScore) > 0);


        Map<String, ?> csaReportData = (Map<String, ?>) cqiReportData.get(CodingStyleAnalyzeReport.NAME);
        Object csaScore = csaReportData.get("csaScore");
        assertTrue(((int) csaScore) < 0, "There are global variables, so the score should be negative.");
    }

    private void mockServiceDrivers() throws IOException {
        when(submissionServiceDriver.getSubmission(
                problemId, studentId, submission.getId())).thenReturn(SubmissionView.fromEntity(submission));
        when(problemServiceDriver.getProblem(problem.getId())).thenReturn(ProblemView.fromEntity(problem));
        when(problemServiceDriver.getTestcases(problem.getId())).thenReturn(testCases);
        mockDownloadRequests();
    }

    private void mockDownloadRequests() throws IOException {
        String zippedSubmittedCodesFileName = String.format(zippedSubmittedCodesFileNameFormat, "AC");
        byte[] zippedSubmittedCodesBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedSubmittedCodesFileName));
        when(submissionServiceDriver.downloadSubmittedCodes(
                problemId, studentId, submission.getId(), submission.getSubmittedCodesFileId()))
                .thenReturn(new FileResource(zippedSubmittedCodesFileName,
                        zippedSubmittedCodesBytes.length,
                        new ByteArrayInputStream(zippedSubmittedCodesBytes)));

        byte[] zippedProvidedCodeBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedProvidedCodesFileName));
        when(problemServiceDriver.downloadProvidedCodes(problem.getId(), problem.getProvidedCodesFileId()))
                .thenReturn(new FileResource(zippedProvidedCodesFileName, zippedProvidedCodeBytes.length,
                        new ByteArrayInputStream(zippedProvidedCodeBytes)));

        byte[] zippedTestcaseInputsBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedTestcaseIOsFileName));
        when(problemServiceDriver.downloadTestCaseIOs(problem.getId(), problem.getTestcaseIOsFileId()))
                .thenReturn(new FileResource(zippedTestcaseIOsFileName,
                        zippedTestcaseInputsBytes.length,
                        new ByteArrayInputStream(zippedTestcaseInputsBytes)));

    }

    private VerdictIssuedEvent captureVerdictIssuedEvent() {
        ArgumentCaptor<VerdictIssuedEvent> argumentCaptor = ArgumentCaptor.forClass(VerdictIssuedEvent.class);
        verify(verdictPublisher).publish(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }
}
