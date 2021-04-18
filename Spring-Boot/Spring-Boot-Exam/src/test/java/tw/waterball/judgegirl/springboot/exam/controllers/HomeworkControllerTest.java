package tw.waterball.judgegirl.springboot.exam.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.stubs.ProblemStubs;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.verdict.Judge;
import tw.waterball.judgegirl.entities.submission.verdict.ProgramProfile;
import tw.waterball.judgegirl.entities.submission.verdict.Verdict;
import tw.waterball.judgegirl.examservice.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.examservice.domain.usecases.homework.CreateHomeworkUseCase;
import tw.waterball.judgegirl.problemapi.clients.FakeProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.exam.SpringBootExamApplication;
import tw.waterball.judgegirl.springboot.exam.view.HomeworkProgressView;
import tw.waterball.judgegirl.springboot.exam.view.HomeworkProgressView.BestRecord;
import tw.waterball.judgegirl.springboot.exam.view.HomeworkView;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.entities.problem.JudgeStatus.AC;
import static tw.waterball.judgegirl.entities.problem.JudgeStatus.CE;
import static tw.waterball.judgegirl.springboot.exam.controllers.ExamControllerTest.LANG_ENV;

@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootExamApplication.class)
public class HomeworkControllerTest extends AbstractSpringBootTest {

    public static final int PROBLEM1_ID = 1;
    public static final int PROBLEM2_ID = 2;
    private static final String HOMEWORK_NAME = "homeworkName";
    public static final int STUDENT_ID = 11;
    private static final String HOMEWORK_PATH = "/api/homework";
    private static final String HOMEWORK_PROGRESS_PATH = "/api/students/{studentId}/homework/{homeworkId}/progress";
    private static final String SUBMISSION1_ID = "1";
    private static final String SUBMISSION2_ID = "2";

    @Autowired
    private FakeProblemServiceDriver problemServiceDriver;

    @MockBean
    private SubmissionServiceDriver submissionServiceDriver;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @AfterEach
    void cleanUp() {
        problemServiceDriver.clear();
        homeworkRepository.deleteAll();
    }

    @Test
    public void GivenThreeProblemsCreated_WhenCreateHomeworkThatIncludesManyProblems_ShouldCreateSuccessfully() throws Exception {
        Integer[] problemIds = {0, 1, 2};
        createProblems(problemIds);

        HomeworkView homework = createHomeworkAndGet(HOMEWORK_NAME, problemIds);

        homeworkShouldIncludeProblemIds(homework, problemIds);
    }

    @DisplayName("Given created two problems [0, 1], " +
            "When create homework to include two problem by ids [0, 1, 2] " +
            "Should respond [0, 1]")
    @Test
    public void testAddProblemsIntoHomeworkByNonExistingProblemIdAndTwoCreatedProblemIds() throws Exception {
        createProblems(0, 1);

        Integer[] problemIds = {0, 1, 2};
        HomeworkView homework = createHomeworkAndGet(HOMEWORK_NAME, problemIds);

        homeworkShouldIncludeProblemIds(homework, 0, 1);
    }

    @Test
    public void GivenOneHomeworkCreated_WhenGetHomeworkById_ShouldRespondHomework() throws Exception {
        HomeworkView homework = createHomeworkAndGet(HOMEWORK_NAME);

        HomeworkView actualHomework = getBody(getHomework(homework.id)
                .andExpect(status().isOk()), HomeworkView.class);

        assertEquals(homework, actualHomework);
    }

    @Test
    public void WhenGetHomeworkByNonExistingHomeworkId_ShouldRespondNotFound() throws Exception {
        int nonExistingHomeworkId = 123123;
        getHomework(nonExistingHomeworkId)
                .andExpect(status().isNotFound());
    }

    @DisplayName("Given created two problems [1, 2] with submission [AC , CE] add into created homework" +
            "When get homework progress by studentId [11] and homework id" +
            "Should two best record [AC, CE] in homework progress")
    @Test
    public void testGetHomeworkProgressOverview() throws Exception {
        HomeworkView homework = addTwoProblemsWithSubmissionIntoCreatedHomeWorkAndGet();

        HomeworkProgressView homeworkProgress = getHomeworkProgress(STUDENT_ID, homework.id);

        homeworkProgressShouldIncludeTwoBestRecord(homework, homeworkProgress);
    }

    private void homeworkProgressShouldIncludeTwoBestRecord(HomeworkView homework, HomeworkProgressView homeworkProgress) {
        assertEquals(homework, homeworkProgress.homework);
        Map<Integer, BestRecord> progress = homeworkProgress.progress;
        assertEquals(2, progress.size());
        assertEquals(AC, progress.get(PROBLEM1_ID).getBestRecord().getSummaryStatus());
        assertEquals(CE, progress.get(PROBLEM2_ID).getBestRecord().getSummaryStatus());
    }

    private HomeworkView addTwoProblemsWithSubmissionIntoCreatedHomeWorkAndGet() throws Exception {
        createProblems(PROBLEM1_ID, PROBLEM2_ID);
        SubmissionView submissionAC = generateSubmissionAndGet(SUBMISSION1_ID, STUDENT_ID, PROBLEM1_ID, AC, AC);
        Mockito.when(submissionServiceDriver.findBestRecord(PROBLEM1_ID, STUDENT_ID))
                .thenReturn(submissionAC);
        SubmissionView submissionCE = generateSubmissionAndGet(SUBMISSION2_ID, STUDENT_ID, PROBLEM2_ID, CE, CE, CE);
        Mockito.when(submissionServiceDriver.findBestRecord(PROBLEM2_ID, STUDENT_ID))
                .thenReturn(submissionCE);
        return createHomeworkAndGet(HOMEWORK_NAME, PROBLEM1_ID, PROBLEM2_ID);
    }

    private HomeworkView createHomeworkAndGet(String homeworkName, Integer... problemIds) throws Exception {
        return getBody(createHomework(homeworkName, problemIds), HomeworkView.class);
    }

    private ResultActions createHomework(String homeworkName, Integer... problemIds) throws Exception {
        CreateHomeworkUseCase.Request request = new CreateHomeworkUseCase.Request(homeworkName, Arrays.asList(problemIds));
        return mockMvc.perform(post(HOMEWORK_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk());
    }

    private ResultActions getHomework(int homeworkId) throws Exception {
        return mockMvc.perform(get(HOMEWORK_PATH + "/{homeworkId}", homeworkId));
    }

    private void createProblems(Integer... problemIds) {
        Arrays.stream(problemIds)
                .map(this::createProblem)
                .forEach(problemServiceDriver::addProblemView);
    }

    private ProblemView createProblem(int problemId) {
        Problem problem = ProblemStubs.problemTemplate().build();
        ProblemView problemView = ProblemView.toViewModel(problem);
        problemView.id = problemId;
        return problemView;
    }

    private void homeworkShouldIncludeProblemIds(HomeworkView homework,
                                                 Integer... problemIds) {
        List<Integer> homeworkProblemIds = homework.problemIds;
        assertEquals(problemIds.length, homeworkProblemIds.size());
        for (int index = 0; index < homeworkProblemIds.size(); index++) {
            assertEquals(problemIds[index], homeworkProblemIds.get(index));
        }
    }

    private SubmissionView generateSubmissionAndGet(String submissionId, int studentId, int problemId,
                                                    JudgeStatus... judgeStatuses) {
        Submission submission = new Submission(studentId, problemId, LANG_ENV);
        submission.setId(submissionId);
        List<Judge> judges = Arrays.stream(judgeStatuses)
                .map(this::generateJudgeAndGet)
                .collect(Collectors.toList());
        submission.setVerdict(new Verdict(judges));
        return SubmissionView.toViewModel(submission);
    }

    private Judge generateJudgeAndGet(JudgeStatus judgeStatus) {
        long runtime = System.currentTimeMillis() % 10;
        long memoryUsage = System.currentTimeMillis() % 10;
        int grade = Math.toIntExact(System.currentTimeMillis() % 100) + 1;
        String errorMessage = judgeStatus == AC ? "" : judgeStatus.getFullName();
        ProgramProfile programProfile = new ProgramProfile(runtime, memoryUsage, errorMessage);
        return new Judge("T" + grade, judgeStatus, programProfile, grade);
    }

    private HomeworkProgressView getHomeworkProgress(int studentId, int homeworkId) throws Exception {
        return getBody(mockMvc.perform(get(HOMEWORK_PROGRESS_PATH, studentId, homeworkId))
                .andExpect(status().isOk()), HomeworkProgressView.class);
    }

}