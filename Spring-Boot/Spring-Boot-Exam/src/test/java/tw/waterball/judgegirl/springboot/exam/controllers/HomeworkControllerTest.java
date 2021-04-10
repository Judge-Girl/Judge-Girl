package tw.waterball.judgegirl.springboot.exam.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.stubs.ProblemStubs;
import tw.waterball.judgegirl.examservice.domain.usecases.homework.CreateHomeworkUseCase;
import tw.waterball.judgegirl.problemapi.clients.FakeProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.exam.SpringBootExamApplication;
import tw.waterball.judgegirl.springboot.exam.view.HomeworkView;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootExamApplication.class)
public class HomeworkControllerTest extends AbstractSpringBootTest {

    private static final String HOMEWORK_NAME = "homeworkName";
    private static final String HOMEWORK_PATH = "/api/homework";

    @Autowired
    private FakeProblemServiceDriver problemServiceDriver;

    @MockBean
    private SubmissionServiceDriver submissionServiceDriver;

    @AfterEach
    void cleanUp() {
        problemServiceDriver.clear();
    }

    @Test
    public void GivenThreeProblemsCreated_WhenCreateHomeworkWithProblemIds_ShouldCreateSuccessfully() throws Exception {
        Integer[] createProblemIds = {1, 2, 3};
        createProblems(createProblemIds);

        HomeworkView homework = getBody(createHomework(HOMEWORK_NAME, createProblemIds)
                .andExpect(status().isOk()), HomeworkView.class);

        List<String> homeworkProblemIds = homework.problemIds;
        assertEquals(createProblemIds.length, homeworkProblemIds.size());
        for (int i = 0; i < homeworkProblemIds.size(); i++) {
            assertEquals(String.valueOf(createProblemIds[i]), homeworkProblemIds.get(i));
        }
    }

    @DisplayName("Given created two problems [0, 1], " +
            "When create homework with two problems id [0, 1] and a non existing problem id 2, " +
            "Should respond two problems id in the homework")
    @Test
    public void testAddProblemsIntoHomeworkByNonExistingProblemIdAndTwoCreatedProblemIds() throws Exception {
        Integer[] createProblemIds = {0, 1};
        createProblems(createProblemIds);

        int nonExistingProblemId = 2;
        Integer[] testCaseProblemIds = {0, 1, nonExistingProblemId};
        HomeworkView homework = getBody(createHomework(HOMEWORK_NAME, testCaseProblemIds)
                .andExpect(status().isOk()), HomeworkView.class);

        List<String> homeworkProblemIds = homework.problemIds;
        assertEquals(createProblemIds.length, homeworkProblemIds.size());
        for (int index = 0; index < homeworkProblemIds.size(); index++) {
            assertEquals(String.valueOf(createProblemIds[index]), homeworkProblemIds.get(index));
        }
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

    @Test
    public void GivenOneHomeworkCreated_WhenGetHomeworkById_ShouldRespondHomework() throws Exception {
        HomeworkView homework = createHomeworkAndGet();

        HomeworkView body = getBody(getHomework(homework.id).andExpect(status().isOk()), HomeworkView.class);

        assertEquals(homework, body);
    }

    @Test
    public void WhenGetHomeworkByNonExistingHomeworkId_ShouldRespondNotFound() throws Exception {
        int nonExistingHomeworkId = 123123;
        getHomework(nonExistingHomeworkId)
                .andExpect(status().isNotFound());
    }

    private HomeworkView createHomeworkAndGet() throws Exception {
        return getBody(createHomework(), HomeworkView.class);
    }

    private ResultActions createHomework() throws Exception {
        Integer[] problemIds = {1, 2, 3};
        return createHomework(HOMEWORK_NAME, problemIds);
    }

    private ResultActions createHomework(String homeworkName, Integer... problemIds) throws Exception {
        CreateHomeworkUseCase.Request request = new CreateHomeworkUseCase.Request(homeworkName, Arrays.asList(problemIds));
        return mockMvc.perform(post(HOMEWORK_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions getHomework(int homeworkId) throws Exception {
        return mockMvc.perform(get(HOMEWORK_PATH + "/{homeworkId}", homeworkId));
    }

}