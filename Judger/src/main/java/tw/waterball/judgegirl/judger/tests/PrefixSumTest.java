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
import tw.waterball.judgegirl.entities.submission.Bag;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.verdict.Judge;
import tw.waterball.judgegirl.entities.submission.verdict.ProgramProfile;
import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.judger.CCJudger;
import tw.waterball.judgegirl.judger.DefaultCCJudgerFactory;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// TODO the test should be parameterized in the future
@SuppressWarnings("SameParameterValue")
public class PrefixSumTest {
    private final static String zippedProvidedCodesFileName = "/judgeCases/prefixsum/provided.zip";
    private final static String zippedTestcaseIOsFileName = "/judgeCases/prefixsum/io.zip";
    private final static String zippedSubmittedCodesFileNameFormat = "/judgeCases/prefixsum/%s/submitted.zip";
    private final static int MEMORY_LIMIT = 128 << 20;
    private static final int problemId = 1;
    public static final String COMPILATION_SCRIPT = "gcc -std=c99 -O2 -pthread prefixsum-seq.c secret.c";
    private static final List<Testcase> testcases = Arrays.asList(
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
    private static final Problem problem = Problem.builder()
            .id(problemId).title("Prefix Sum")
            .description("Ignored")
            .languageEnv(languageEnv.getName(), languageEnv)
            .outputMatchPolicyPluginTag(AllMatchPolicyPlugin.TAG)
            .tag("Ignored")
            .testcaseIOsFileId("testcaseIOsFileId")
            .testcases(testcases)
            .build();

    private static final int studentId = 1;
    private static final Map<String, String> submissionBag = singletonMap("BagKey", "BagKey");
    private static final Submission submission = new Submission(studentId, problem.getId(), languageEnv.getName(), "fileId") {{
        setBag(new Bag(submissionBag));
    }};
    private ProblemServiceDriver problemServiceDriver;
    private SubmissionServiceDriver submissionServiceDriver;
    private VerdictPublisher verdictPublisher;
    private CCJudger judger;

    @BeforeEach
    void setup() {
        problemServiceDriver = mock(ProblemServiceDriver.class);
        submissionServiceDriver = mock(SubmissionServiceDriver.class);
        verdictPublisher = mock(VerdictPublisher.class);

        judger = DefaultCCJudgerFactory.create("/judger-layout.yaml",
                problemServiceDriver, submissionServiceDriver, verdictPublisher);
    }

    @Test
    void judge_AC() throws IOException {
        submission.setId("Submission_AC");
        mockServiceDrivers(JudgeStatus.AC);

        judger.judge(studentId, problemId, submission.getId());

        verifyACPublished();
    }

    private void verifyACPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        for (int i = 0; i < testcases.size(); i++) {
            Judge judge = event.getVerdict().getJudges().get(i);
            Testcase testCase = testcases.get(i);
            assertEquals(JudgeStatus.AC, judge.getStatus(), event.toString());
            assertEquals(testCase.getGrade(), judge.getGrade());
            assertEquals(testCase.getName(), judge.getTestcaseName());
            ProgramProfile profile = judge.getProgramProfile();
            assertEquals("", profile.getErrorMessage(), "Error message should be empty if AC");
            assertTrue(profile.getRuntime() <= testCase.getTimeLimit());
            assertTrue(profile.getMemoryUsage() <= testCase.getMemoryLimit());
        }
        assertEquals(problemId, event.getProblemId());
        assertEquals(problem.getTitle(), event.getProblemTitle());
        assertEquals(submission.getId(), event.getSubmissionId());
        assertNull(event.getVerdict().getCompileErrorMessage(), "Compile error message should be null if the compile succeeded.");
        assertEquals(submissionBag, event.getSubmissionBag(), "The bag should also be published");
    }


    @Test
    void judge_CE() throws IOException {
        submission.setId("Submission_CE");
        mockServiceDrivers(JudgeStatus.CE);

        judger.judge(studentId, problemId, submission.getId());

        verifyCEPublished();
    }

    private void verifyCEPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        for (int i = 0; i < testcases.size(); i++) {
            Judge judge = event.getVerdict().getJudges().get(i);
            Testcase testCase = testcases.get(i);
            assertEquals(JudgeStatus.CE, judge.getStatus(), event.toString());
            assertEquals(0, judge.getGrade());
            assertEquals(testCase.getName(), judge.getTestcaseName());
            ProgramProfile profile = judge.getProgramProfile();
            assertEquals(0, profile.getRuntime());
            assertEquals(0, profile.getMemoryUsage());
        }
        assertEquals(problemId, event.getProblemId());
        assertEquals(problem.getTitle(), event.getProblemTitle());
        assertEquals(submission.getId(), event.getSubmissionId());
        assertNotNull(event.getVerdict().getCompileErrorMessage(), "Compile error message should not be null if the compile failed.");
    }


    @Test
    void judge_TLE() throws IOException {
        submission.setId("Submission_TLE");
        mockServiceDrivers(JudgeStatus.TLE);

        judger.judge(studentId, problemId, submission.getId());

        verifyTLEPublished();
    }

    private void verifyTLEPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        for (int i = 0; i < testcases.size(); i++) {
            Judge judge = event.getVerdict().getJudges().get(i);
            Testcase testCase = testcases.get(i);
            assertEquals(JudgeStatus.TLE, judge.getStatus(), event.toString());
            assertEquals(0, judge.getGrade());
            assertEquals(testCase.getName(), judge.getTestcaseName());
            ProgramProfile profile = judge.getProgramProfile();
            assertTrue(profile.getRuntime() > testCase.getTimeLimit());
        }
        assertEquals(problemId, event.getProblemId());
        assertEquals(problem.getTitle(), event.getProblemTitle());
        assertEquals(submission.getId(), event.getSubmissionId());
        assertNull(event.getVerdict().getCompileErrorMessage(), "Compile error message should be null if the compile succeeded.");
    }


    @Test
    void judge_WA() throws IOException {
        submission.setId("Submission_WA");
        mockServiceDrivers(JudgeStatus.WA);

        judger.judge(studentId, problemId, submission.getId());

        verifyWAPublished();
    }

    private void verifyWAPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        for (int i = 0; i < testcases.size(); i++) {
            Judge judge = event.getVerdict().getJudges().get(i);
            Testcase testCase = testcases.get(i);
            assertEquals(JudgeStatus.WA, judge.getStatus(), event.toString());
            assertEquals(0, judge.getGrade());
            assertEquals(testCase.getName(), judge.getTestcaseName());
        }
        assertEquals(problemId, event.getProblemId());
        assertEquals(problem.getTitle(), event.getProblemTitle());
        assertEquals(submission.getId(), event.getSubmissionId());
        assertNull(event.getVerdict().getCompileErrorMessage(), "Compile error message should be null if the compile succeeded.");
    }

    private void mockServiceDrivers(JudgeStatus judgeStatus) throws IOException {
        when(submissionServiceDriver.getSubmission(
                problemId, studentId, submission.getId())).thenReturn(SubmissionView.toViewModel(submission));
        when(problemServiceDriver.getProblem(problem.getId())).thenReturn(ProblemView.toViewModel(problem));

        mockDownloadRequests(judgeStatus);
    }

    private void mockDownloadRequests(JudgeStatus status) throws IOException {
        String zippedSubmittedCodesFileName = String.format(zippedSubmittedCodesFileNameFormat, status);
        byte[] zippedSubmittedCodesBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedSubmittedCodesFileName));
        when(submissionServiceDriver.downloadSubmittedCodes(
                problemId, studentId, submission.getId(), submission.getSubmittedCodesFileId()))
                .thenReturn(new FileResource(zippedSubmittedCodesFileName,
                        zippedSubmittedCodesBytes.length,
                        new ByteArrayInputStream(zippedSubmittedCodesBytes)));

        byte[] zippedProvidedCodeBytes = IOUtils.toByteArray(getClass().getResourceAsStream(zippedProvidedCodesFileName));
        when(problemServiceDriver.downloadProvidedCodes(problem.getId(), languageEnv.getName(), languageEnv.getProvidedCodesFileId()))
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
