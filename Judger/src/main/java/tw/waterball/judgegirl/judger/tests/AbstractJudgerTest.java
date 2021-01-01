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
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.entities.stubs.Stubs;
import tw.waterball.judgegirl.entities.submission.Judge;
import tw.waterball.judgegirl.entities.submission.ProgramProfile;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.judger.CCJudger;
import tw.waterball.judgegirl.judger.DefaultCCJudgerFactory;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionapi.views.VerdictIssuedEvent;

import java.io.*;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AbstractJudgerTest that helps mock the service drivers and execute the judger.
 * The test will locate the provided codes and testcaseIOs files
 * under 'resources/judgeCases/{problemName}/' and
 * 'resources/judgeCases/{problemName}/' directories, respectively.
 * And locate the submitted code files according to different JudgeStatus under
 * 'resources/judgeCases/{problemName}/{judgeStatus (Uppercase)}/' directory.
 */
public abstract class AbstractJudgerTest {
    private final static String providedCodesHomePathFormat = "/judgeCases/%s";
    private final static String testcaseIOsHomePathFormat = "/judgeCases/%s";
    private final static String submittedCodesHomePathFormat = "/judgeCases/%s/%s";
    private int studentId = 1234;
    private int problemId;
    private Problem problem;
    private List<Testcase> testCases;
    private Submission submission;
    private ProblemServiceDriver problemServiceDriver;
    private SubmissionServiceDriver submissionServiceDriver;
    private VerdictPublisher verdictPublisher;
    private CCJudger judger;

    @BeforeEach
    void setup() {
        problem = getProblem(Stubs.problemTemplateBuilder());
        problemId = problem.getId();
        testCases = getTestCases();
        submission = new Submission(studentId, problem.getId(), "fileId");

        problemServiceDriver = mock(ProblemServiceDriver.class);
        submissionServiceDriver = mock(SubmissionServiceDriver.class);
        verdictPublisher = mock(VerdictPublisher.class);

        judger = DefaultCCJudgerFactory.create("/judger-layout.yaml",
                problemServiceDriver, submissionServiceDriver, verdictPublisher);
    }

    protected abstract Problem getProblem(Problem.ProblemBuilder problemBuilder);

    protected abstract List<Testcase> getTestCases();

    @Test
    void judge_AC() throws IOException {
        submission.setId("Submission_AC");
        if (submittedCodeExists(JudgeStatus.AC)) {
            mockServiceDrivers(JudgeStatus.AC);
            judger.judge(studentId, problemId, submission.getId());
            verifyACPublished();
        }
    }


    @Test
    void judge_CE() throws IOException {
        submission.setId("Submission_CE");
        if (submittedCodeExists(JudgeStatus.CE)) {
            mockServiceDrivers(JudgeStatus.CE);
            judger.judge(studentId, problemId, submission.getId());
            verifyCEPublished();
        }
    }


    @Test
    void judge_TLE() throws IOException {
        submission.setId("Submission_TLE");
        if (submittedCodeExists(JudgeStatus.TLE)) {
            mockServiceDrivers(JudgeStatus.TLE);
            judger.judge(studentId, problemId, submission.getId());
            verifyTLEPublished();
        }
    }

    @Test
    void judge_WA() throws IOException {
        submission.setId("Submission_WA");
        if (submittedCodeExists(JudgeStatus.WA)) {
            mockServiceDrivers(JudgeStatus.WA);
            judger.judge(studentId, problemId, submission.getId());
            verifyWAPublished();
        }

    }

    private void verifyACPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        for (int i = 0; i < testCases.size(); i++) {
            Judge judge = event.getJudges().get(i);
            Testcase testCase = testCases.get(i);
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
        assertNull(event.getCompileErrorMessage(), "Compile error message should be null if the compile succeeded.");
    }

    private void verifyCEPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        for (int i = 0; i < testCases.size(); i++) {
            Judge judge = event.getJudges().get(i);
            Testcase testCase = testCases.get(i);
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
        assertNotNull(event.getCompileErrorMessage(), "Compile error message should not be null if the compile failed.");
    }

    private void verifyTLEPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        for (int i = 0; i < testCases.size(); i++) {
            Judge judge = event.getJudges().get(i);
            Testcase testCase = testCases.get(i);
            assertEquals(JudgeStatus.TLE, judge.getStatus(), event.toString());
            assertEquals(0, judge.getGrade());
            assertEquals(testCase.getName(), judge.getTestcaseName());
            ProgramProfile profile = judge.getProgramProfile();
            assertTrue(profile.getRuntime() > testCase.getTimeLimit());
        }
        assertEquals(problemId, event.getProblemId());
        assertEquals(problem.getTitle(), event.getProblemTitle());
        assertEquals(submission.getId(), event.getSubmissionId());
        assertNull(event.getCompileErrorMessage(), "Compile error message should be null if the compile succeeded.");
    }


    private void verifyWAPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        for (int i = 0; i < testCases.size(); i++) {
            Judge judge = event.getJudges().get(i);
            Testcase testCase = testCases.get(i);
            assertEquals(JudgeStatus.WA, judge.getStatus(), event.toString());
            assertEquals(0, judge.getGrade());
            assertEquals(testCase.getName(), judge.getTestcaseName());
        }
        assertEquals(problemId, event.getProblemId());
        assertEquals(problem.getTitle(), event.getProblemTitle());
        assertEquals(submission.getId(), event.getSubmissionId());
        assertNull(event.getCompileErrorMessage(), "Compile error message should be null if the compile succeeded.");
    }

    private boolean submittedCodeExists(JudgeStatus status) {
        String fileName = String.format(submittedCodesHomePathFormat, problem.getTitle(), status);
        return getClass().getResourceAsStream(fileName) != null;
    }

    private void mockServiceDrivers(JudgeStatus judgeStatus) throws IOException {
        when(submissionServiceDriver.getSubmission(
                problemId, studentId, submission.getId())).thenReturn(SubmissionView.fromEntity(submission));
        when(problemServiceDriver.getProblem(problem.getId())).thenReturn(ProblemView.fromEntity(problem));
        when(problemServiceDriver.getTestcases(problem.getId())).thenReturn(testCases);

        mockDownloadRequests(judgeStatus);
    }

    private void mockDownloadRequests(JudgeStatus status) throws IOException {
        mockDownloadSubmittedCodes(status);
        mockDownloadProvidedCodes();
        mockDownloadTestcaseIOs();
    }

    private void mockDownloadSubmittedCodes(JudgeStatus status) throws IOException {
        String submittedCodesHomePath = String.format(submittedCodesHomePathFormat, problem.getTitle(), status);
        File submittedCodesZip = zipDirectory(submittedCodesHomePath);
        byte[] zippedSubmittedCodesBytes = IOUtils.toByteArray(new FileInputStream(submittedCodesZip));
        when(submissionServiceDriver.downloadSubmittedCodes(
                problemId, studentId, submission.getId(), submission.getSubmittedCodesFileId()))
                .thenReturn(new FileResource(submittedCodesHomePath,
                        zippedSubmittedCodesBytes.length,
                        new ByteArrayInputStream(zippedSubmittedCodesBytes)));
    }

    private void mockDownloadProvidedCodes() throws IOException {
        String providedCodesHomePath = String.format(providedCodesHomePathFormat, problem.getTitle());
        File providedCodesZip = zipDirectory(providedCodesHomePath);
        byte[] zippedProvidedCodesBytes = IOUtils.toByteArray(new FileInputStream(providedCodesZip));
        when(problemServiceDriver.downloadProvidedCodes(problem.getId(), problem.getProvidedCodesFileId()))
                .thenReturn(new FileResource(providedCodesHomePath, zippedProvidedCodesBytes.length,
                        new ByteArrayInputStream(zippedProvidedCodesBytes)));
    }

    private void mockDownloadTestcaseIOs() throws IOException {
        String testcaseIOsHomePath = String.format(testcaseIOsHomePathFormat, problem.getTitle());
        File testcaseIOsZip = zipDirectory(testcaseIOsHomePath);
        byte[] zippedTestcaseIOsBytes = IOUtils.toByteArray(new FileInputStream(testcaseIOsZip));
        when(problemServiceDriver.downloadTestCaseIOs(problem.getId(), problem.getTestcaseIOsFileId()))
                .thenReturn(new FileResource(testcaseIOsHomePath,
                        zippedTestcaseIOsBytes.length,
                        new ByteArrayInputStream(zippedTestcaseIOsBytes)));
    }

    private File zipDirectory(String directoryResourcePath) throws IOException {
        URL directoryURL = getClass().getResource(directoryResourcePath);
        File submittedCodesZip = File.createTempFile("judge-girl", ".zip");
        submittedCodesZip.deleteOnExit();
        ZipUtils.zip(directoryURL, new FileOutputStream(submittedCodesZip));
        return submittedCodesZip;
    }

    private VerdictIssuedEvent captureVerdictIssuedEvent() {
        ArgumentCaptor<VerdictIssuedEvent> argumentCaptor = ArgumentCaptor.forClass(VerdictIssuedEvent.class);
        verify(verdictPublisher).publish(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }
}
