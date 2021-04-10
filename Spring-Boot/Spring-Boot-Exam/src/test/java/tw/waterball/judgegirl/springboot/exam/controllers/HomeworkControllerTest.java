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
import tw.waterball.judgegirl.examservice.domain.repositories.HomeworkRepository;
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

        homeworkShouldIncludeProblemIds(homework, problemIds.length, problemIds);
    }

    @DisplayName("Given created two problems [0, 1], " +
            "When create homework to include two problem by ids [0, 1, 2] " +
            "Should respond [0, 1]")
    @Test
    public void testAddProblemsIntoHomeworkByNonExistingProblemIdAndTwoCreatedProblemIds() throws Exception {
        createProblems(0, 1);

        Integer[] problemIds = {0, 1, 2};
        HomeworkView homework = createHomeworkAndGet(HOMEWORK_NAME, problemIds);

        homeworkShouldIncludeProblemIds(homework, 2, problemIds);
    }

    @Test
    public void GivenOneHomeworkCreated_WhenGetHomeworkById_ShouldRespondHomework() throws Exception {
        HomeworkView homework = createHomeworkAndGet(HOMEWORK_NAME);

        HomeworkView actualHomework = getBody(getHomework(homework.id)
                .andExpect(status().isOk()), HomeworkView.class);

        assertEquals(homework, actualHomework);
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

    @Test
    public void WhenGetHomeworkByNonExistingHomeworkId_ShouldRespondNotFound() throws Exception {
        int nonExistingHomeworkId = 123123;
        getHomework(nonExistingHomeworkId)
                .andExpect(status().isNotFound());
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
                                                 int expectIdAmount,
                                                 Integer... problemIds) {
        List<Integer> homeworkProblemIds = homework.problemIds;
        assertEquals(expectIdAmount, homeworkProblemIds.size());
        for (int index = 0; index < homeworkProblemIds.size(); index++) {
            assertEquals(problemIds[index], homeworkProblemIds.get(index));
        }
    }
}