package tw.waterball.judgegirl.springboot.academy.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.academy.domain.usecases.homework.CreateHomeworkUseCase;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.stubs.ProblemStubs;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.problemapi.clients.FakeProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.academy.SpringBootAcademyApplication;
import tw.waterball.judgegirl.springboot.academy.view.HomeworkProgress;
import tw.waterball.judgegirl.springboot.academy.view.HomeworkProgress.BestRecord;
import tw.waterball.judgegirl.springboot.academy.view.HomeworkView;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.primitives.stubs.SubmissionStubBuilder.submission;
import static tw.waterball.judgegirl.springboot.academy.controllers.ExamControllerTest.LANG_ENV;

@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootAcademyApplication.class)
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
    public void GivenOneHomeworkCreated_WhenDeleteHomeworkById_ShouldSucceed() throws Exception {
        var homework = createHomeworkAndGet(HOMEWORK_NAME);

        deleteHomework(homework.id);

        assertTrue(homeworkRepository.findHomeworkById(homework.id).isEmpty());
    }

    @Test
    public void Given3HomeworkCreated_WhenGetAllHomework_ShouldRespondHomeworkListOfSize3() throws Exception {
        var homeworkList = createHomeworkListAndGet(3);

        var actualHomeworkList = getAllHomework();

        assertEquals(homeworkList, actualHomeworkList);
        range(0, homeworkList.size())
                .forEach(i -> assertEquals(homeworkList.get(i), actualHomeworkList.get(i)));
    }

    @Test
    public void WhenGetHomeworkByNonExistingHomeworkId_ShouldRespondNotFound() throws Exception {
        int nonExistingHomeworkId = 123123;
        getHomework(nonExistingHomeworkId)
                .andExpect(status().isNotFound());
    }

    @DisplayName("Given a homework consists of two problems [1, 2] and the student achieved AC in 1 and CE in 2," +
            "When get the student's homework progress, " +
            "Should respond the two best records [AC, CE] within the homework progress")
    @Test
    public void testGetHomeworkProgress() throws Exception {
        var homework = createHomeworkConsistsOfProblems(PROBLEM1_ID, PROBLEM2_ID);
        var bestRecord1 = achieveBestRecord(PROBLEM1_ID, submission("AC").AC(1, 1, 100).build(STUDENT_ID, PROBLEM1_ID, LANG_ENV));
        var bestRecord2 = achieveBestRecord(PROBLEM2_ID, submission("CE").CE().build(STUDENT_ID, PROBLEM2_ID, LANG_ENV));

        HomeworkProgress homeworkProgress = getHomeworkProgress(STUDENT_ID, homework.id);

        assertEquals(homework, homeworkProgress.homework);
        homeworkProgressShouldIncludeTwoBestRecords(homework, homeworkProgress, bestRecord1, bestRecord2);
    }

    private HomeworkView createHomeworkConsistsOfProblems(Integer... problemIds) throws Exception {
        createProblems(problemIds);
        return createHomeworkAndGet(HOMEWORK_NAME, problemIds);
    }

    private SubmissionView achieveBestRecord(int problemId, Submission submission) {
        int studentId = submission.getStudentId();
        var bestRecord = SubmissionView.toViewModel(submission);
        when(submissionServiceDriver.findBestRecord(problemId, studentId)).thenReturn(bestRecord);
        return bestRecord;
    }

    private void homeworkProgressShouldIncludeTwoBestRecords(HomeworkView homework,
                                                             HomeworkProgress homeworkProgress,
                                                             SubmissionView... submissionViews) {
        assertEquals(homework, homeworkProgress.homework);
        Map<Integer, BestRecord> progress = homeworkProgress.progress;
        assertEquals(submissionViews.length, progress.size());
        for (SubmissionView submissionView : submissionViews) {
            JudgeStatus summaryStatus = submissionView.verdict.getSummaryStatus();
            JudgeStatus actualSummaryStatus = progress.get(submissionView.problemId)
                    .getBestRecord().getSummaryStatus();
            assertEquals(summaryStatus, actualSummaryStatus);
        }
    }

    @SneakyThrows
    private HomeworkView createHomeworkAndGet(String homeworkName, Integer... problemIds) {
        return getBody(createHomework(homeworkName, problemIds), HomeworkView.class);
    }

    private List<HomeworkView> createHomeworkListAndGet(int count)  {
        return range(0, count)
                .mapToObj(i -> createHomeworkAndGet(HOMEWORK_NAME + i))
                .collect(toList());
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

    private List<HomeworkView> getAllHomework() throws Exception {
        return getBody(mockMvc.perform(get(HOMEWORK_PATH)).andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private ResultActions deleteHomework(int homeworkId) throws Exception {
        return mockMvc.perform(delete(HOMEWORK_PATH + "/{homeworkId}", homeworkId))
                .andExpect(status().isOk());
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

    private HomeworkProgress getHomeworkProgress(int studentId, int homeworkId) throws Exception {
        return getBody(mockMvc.perform(get(HOMEWORK_PROGRESS_PATH, studentId, homeworkId))
                .andExpect(status().isOk()), HomeworkProgress.class);
    }

}