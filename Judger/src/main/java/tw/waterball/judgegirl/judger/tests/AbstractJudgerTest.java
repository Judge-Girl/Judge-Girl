package tw.waterball.judgegirl.judger.tests;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.judger.CCJudger;
import tw.waterball.judgegirl.judger.DefaultCCJudgerFactory;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.Testcase;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.verdict.Judge;
import tw.waterball.judgegirl.primitives.submission.verdict.ProgramProfile;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.io.File.createTempFile;
import static java.lang.String.format;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static tw.waterball.judgegirl.commons.utils.ZipUtils.zipDirectoryContents;
import static tw.waterball.judgegirl.submissionapi.views.SubmissionView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class AbstractJudgerTest {
    public static String problemHomePath;
    public static String providedCodesHomeFormat;
    public static String testcaseIOsHomeFormat;
    public static String submittedCodesHomeFormat;
    public static final Language CURRENTLY_ONLY_SUPPORT_C = Language.C;
    private final int studentId = 1234;
    private int problemId;
    private Problem problem;
    private Submission submission;
    private ProblemServiceDriver problemServiceDriver;
    private SubmissionServiceDriver submissionServiceDriver;
    private VerdictPublisher verdictPublisher;
    private CCJudger judger;

    @BeforeAll
    static void beforeAll() {
        problemHomePath = System.getenv("JUDGER_TEST_PROBLEM_HOME");
        if (problemHomePath == null) {
            throw new IllegalArgumentException("Require an env var: JUDGER_TEST_PROBLEM_HOME");
        }
        providedCodesHomeFormat = problemHomePath + "/%s/providedCodes"; // (1: problem's id)
        testcaseIOsHomeFormat = problemHomePath + "/%s/testcases"; // (1: problem's id)
        submittedCodesHomeFormat = problemHomePath + "/%s/%s/submitted"; // (1: problem's id, 2: judge status)
        System.out.printf("Problem home: %s\n", problemHomePath);
    }

    @BeforeEach
    void setup() {
        problem = getProblem();
        problem.setOutputMatchPolicyPluginTag(AllMatchPolicyPlugin.TAG);
        problem.setTestcaseIOsFileId("testcaseIOsFileId");
        problem.getLanguageEnvs().values().forEach(languageEnv -> languageEnv.setProvidedCodesFileId("providedCodesFileId"));
        problem.validate();
        problemId = problem.getId();
        submission = new Submission(studentId, problem.getId(), CURRENTLY_ONLY_SUPPORT_C.toString(), "fileId");

        problemServiceDriver = mock(ProblemServiceDriver.class);
        submissionServiceDriver = mock(SubmissionServiceDriver.class);
        verdictPublisher = mock(VerdictPublisher.class);

        judger = DefaultCCJudgerFactory.create("/judger-layout.yaml",
                problemServiceDriver, submissionServiceDriver, verdictPublisher);
    }

    protected abstract Problem getProblem();

    @Test
    void judge_AC() throws IOException {
        submission.setId("[" + problemId + "] Submission_AC");
        if (submittedCodeExists(JudgeStatus.AC)) {
            mockServiceDrivers(JudgeStatus.AC);
            judger.judge(studentId, problemId, submission.getId());
            verifyACPublished();
        }
    }

    @Test
    void judge_CE() throws IOException {
        submission.setId("[" + problemId + "] Submission_CE");
        if (submittedCodeExists(JudgeStatus.CE)) {
            mockServiceDrivers(JudgeStatus.CE);
            judger.judge(studentId, problemId, submission.getId());
            verifyCEPublished();
        }
    }

    @Test
    void judge_TLE() throws IOException {
        submission.setId("[" + problemId + "] Submission_TLE");
        if (submittedCodeExists(JudgeStatus.TLE)) {
            mockServiceDrivers(JudgeStatus.TLE);
            judger.judge(studentId, problemId, submission.getId());
            verifyTLEPublished();
        }
    }

    @Test
    void judge_WA() throws IOException {
        submission.setId("[" + problemId + "] Submission_WA");
        if (submittedCodeExists(JudgeStatus.WA)) {
            mockServiceDrivers(JudgeStatus.WA);
            judger.judge(studentId, problemId, submission.getId());
            verifyWAPublished();
        }
    }

    private void verifyACPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        Verdict verdict = event.getVerdict();
        for (int i = 0; i < problem.numOfTestcases(); i++) {
            Judge judge = verdict.getJudges().get(i);
            Testcase testCase = problem.getTestcase(i);
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
        assertNull(verdict.getCompileErrorMessage(), "Compile error message should be null if the compile succeeded.");
    }

    private void verifyCEPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        Verdict verdict = event.getVerdict();
        assertEquals(problemId, event.getProblemId());
        assertEquals(problem.getTitle(), event.getProblemTitle());
        assertEquals(submission.getId(), event.getSubmissionId());
        assertTrue(verdict.isCompileError());
        assertNotNull(verdict.getCompileErrorMessage(), "Compile error message should not be null if the compile failed.");
    }

    private void verifyTLEPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        Verdict verdict = event.getVerdict();
        for (int i = 0; i < problem.numOfTestcases(); i++) {
            Judge judge = verdict.getJudges().get(i);
            Testcase testCase = problem.getTestcase(i);
            assertEquals(JudgeStatus.TLE, judge.getStatus(), event.toString());
            assertEquals(0, judge.getGrade());
            assertEquals(testCase.getName(), judge.getTestcaseName());
            ProgramProfile profile = judge.getProgramProfile();
            assertTrue(profile.getRuntime() > testCase.getTimeLimit());
        }
        assertEquals(problemId, event.getProblemId());
        assertEquals(problem.getTitle(), event.getProblemTitle());
        assertEquals(submission.getId(), event.getSubmissionId());
        assertNull(verdict.getCompileErrorMessage(), "Compile error message should be null if the compile succeeded.");
    }

    private void verifyWAPublished() {
        VerdictIssuedEvent event = captureVerdictIssuedEvent();
        Verdict verdict = event.getVerdict();
        for (int i = 0; i < problem.numOfTestcases(); i++) {
            Judge judge = verdict.getJudges().get(i);
            Testcase testCase = problem.getTestcase(i);
            assertEquals(JudgeStatus.WA, judge.getStatus(), event.toString());
            assertEquals(0, judge.getGrade());
            assertEquals(testCase.getName(), judge.getTestcaseName());
        }
        assertEquals(problemId, event.getProblemId());
        assertEquals(problem.getTitle(), event.getProblemTitle());
        assertEquals(submission.getId(), event.getSubmissionId());
        assertNull(verdict.getCompileErrorMessage(), "Compile error message should be null if the compile succeeded.");
    }

    private boolean submittedCodeExists(JudgeStatus status) {
        String fileName = format(submittedCodesHomeFormat, problem.getId(), status);
        return Files.exists(Paths.get(fileName));
    }

    private void mockServiceDrivers(JudgeStatus judgeStatus) throws IOException {
        when(submissionServiceDriver.getSubmission(problemId, studentId, submission.getId()))
                .thenReturn(toViewModel(submission));
        when(problemServiceDriver.getProblem(problem.getId()))
                .thenReturn(of(problem).map(ProblemView::toViewModel));

        mockDownloadRequests(judgeStatus);
    }

    private void mockDownloadRequests(JudgeStatus status) throws IOException {
        mockDownloadSubmittedCodes(status);
        mockDownloadProvidedCodes();
        mockDownloadTestcaseIOs();
    }

    private void mockDownloadSubmittedCodes(JudgeStatus status) throws IOException {
        String submittedCodesHomePath = format(submittedCodesHomeFormat, problem.getId(), status);
        byte[] zippedSubmittedCodesBytes = zipDirectory(submittedCodesHomePath);
        when(submissionServiceDriver.downloadSubmittedCodes(
                problemId, studentId, submission.getId(), submission.getSubmittedCodesFileId()))
                .thenReturn(new FileResource(submittedCodesHomePath,
                        zippedSubmittedCodesBytes.length,
                        new ByteArrayInputStream(zippedSubmittedCodesBytes)));
    }

    private void mockDownloadProvidedCodes() throws IOException {
        String providedCodesHomePath = format(providedCodesHomeFormat, problem.getId());
        byte[] zippedProvidedCodesBytes = zipDirectory(providedCodesHomePath);
        when(problemServiceDriver.downloadProvidedCodes(problem.getId(), problem.getLanguageEnv(CURRENTLY_ONLY_SUPPORT_C)))
                .thenReturn(new FileResource(providedCodesHomePath, zippedProvidedCodesBytes.length,
                        new ByteArrayInputStream(zippedProvidedCodesBytes)));
    }

    private void mockDownloadTestcaseIOs() throws IOException {
        String testcaseIOsHomePath = format(testcaseIOsHomeFormat, problem.getId());
        byte[] zippedTestcaseIOsBytes = zipDirectory(testcaseIOsHomePath);
        when(problemServiceDriver.downloadTestCaseIOs(problem.getId(), problem.getTestcaseIOsFileId()))
                .thenReturn(new FileResource(testcaseIOsHomePath,
                        zippedTestcaseIOsBytes.length,
                        new ByteArrayInputStream(zippedTestcaseIOsBytes)));
    }

    private static byte[] zipDirectory(String directoryPath) throws IOException {
        File submittedCodesZip = createTempFile("judge-girl", ".zip");
        submittedCodesZip.deleteOnExit();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        zipDirectoryContents(new File(directoryPath), baos);
        return baos.toByteArray();
    }

    private VerdictIssuedEvent captureVerdictIssuedEvent() {
        ArgumentCaptor<VerdictIssuedEvent> argumentCaptor = ArgumentCaptor.forClass(VerdictIssuedEvent.class);
        verify(verdictPublisher).publish(argumentCaptor.capture());
        return argumentCaptor.getValue();
    }

}
