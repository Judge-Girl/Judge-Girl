package tw.waterball.judgegirl.springboot.exam.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.ExamParticipation;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.ExamParticipationRepository;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;
import tw.waterball.judgegirl.examservice.usecases.CreateExamUseCase;
import tw.waterball.judgegirl.examservice.usecases.CreateQuestionUseCase;
import tw.waterball.judgegirl.problemapi.clients.FakeProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.exam.SpringBootExamApplication;
import tw.waterball.judgegirl.springboot.exam.view.ExamOverview;
import tw.waterball.judgegirl.springboot.exam.view.ExamView;
import tw.waterball.judgegirl.springboot.exam.view.QuestionView;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.commons.utils.DateUtils.afterCurrentTime;
import static tw.waterball.judgegirl.commons.utils.DateUtils.beforeCurrentTime;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootExamApplication.class)
class ExamControllerTest extends AbstractSpringBootTest {

    public static final int PROBLEM_ID = 2;
    public static final int ANOTHER_PROBLEM_ID = 300;

    public static final int NONEXISTING_EXAM_ID = 9999;
    public static final int NONEXISTING_PROBLEM_ID = 9999;


    @Autowired
    ExamRepository examRepository;
    @Autowired
    ExamParticipationRepository examParticipationRepository;
    @Autowired
    FakeProblemServiceDriver problemServiceDriver;
    private ProblemView problem;
    private ProblemView anotherProblem;

    @Configuration
    public static class TestConfig {
        @Bean
        @Primary
        public FakeProblemServiceDriver fakeProblemServiceDriver() {
            return new FakeProblemServiceDriver();
        }
    }


    @BeforeEach
    void setup() {
        problem = new ProblemView();
        problem.setId(PROBLEM_ID);
        problem.setTitle("problem1");
        problemServiceDriver.addProblemView(problem);
        anotherProblem = new ProblemView();
        anotherProblem.setId(ANOTHER_PROBLEM_ID);
        anotherProblem.setTitle("another-problem");
        problemServiceDriver.addProblemView(anotherProblem);
    }


    @AfterEach
    void cleanup() {
        examParticipationRepository.deleteAll();
        examRepository.deleteAll();
        problemServiceDriver.clear();
    }

    @Test
    void WhenCreateExamWithEndTimeAfterStartTime_ShouldSucceed() throws Exception {
        String name = "test-contest", description = "problem statement";
        Date startTime = new Date(), endTime = new Date();
        Exam exam = new Exam(name, startTime, endTime, description);

        createExam(exam)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(name))
                .andExpect(jsonPath("startTime").value(startTime))
                .andExpect(jsonPath("endTime").value(endTime))
                .andExpect(jsonPath("description").value(description));

    }

    @Test
    void WhenCreateExamWithEndTimeBeforeStartTime_ShouldRespondBadRequest() throws Exception {
        String name = "test-contest", description = "problem statement";
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date startTime = cal.getTime(), endTime = new Date();
        Exam exam = new Exam(name, startTime, endTime, description);

        createExam(exam)
                .andExpect(status().isBadRequest());

    }

    @Test
    void GivenStudentParticipatesOnThreeExams_AndTwoOfThemAreUpcoming_WhenGetUpcomingExams_ShouldResponseThoseTwo() throws Exception {
        Date now = new Date();
        Date future = afterCurrentTime(2, HOURS);
        Date past = beforeCurrentTime(2, HOURS);
        ExamView upcoming1 = createExamAndGet(future, future, "upcoming1");
        ExamView upcoming2 = createExamAndGet(future, future, "upcoming2");
        ExamView past1 = createExamAndGet(past, now, "past1");
        createExamParticipation(new ExamParticipation(upcoming1.getId(), 1));
        createExamParticipation(new ExamParticipation(upcoming2.getId(), 1));
        createExamParticipation(new ExamParticipation(past1.getId(), 1));

        List<ExamView> exams = getBody(
                getExams(1, "upcoming")
                        .andExpect(status().isOk()), new TypeReference<>() {
                });

        List<Integer> actualExamIdSet = mapToList(exams, ExamView::getId);
        List<Integer> expectExamIdSet = asList(upcoming1.getId(), upcoming2.getId());
        assertEqualsIgnoreOrder(expectExamIdSet, actualExamIdSet);
    }

    @Test
    void GivenOneExam_WhenCreateQuestionForExistingExam_ShouldRespondTheQuestionAndQuestionShouldBeAddedIntoExam() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        createQuestion(new CreateQuestionUseCase.Request(examView.getId(), PROBLEM_ID, 5, 100, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("examId").value(examView.id))
                .andExpect(jsonPath("problemId").value(PROBLEM_ID))
                .andExpect(jsonPath("quota").value(5))
                .andExpect(jsonPath("score").value(100));

        List<Question> questions = examRepository.findQuestionsInExam(examView.id);
        assertEquals(1, questions.size());
    }

    @Test
    void WhenCreateQuestionForNonExistingExam_ShouldRespondNotFound() throws Exception {
        createQuestion(new CreateQuestionUseCase.Request(NONEXISTING_EXAM_ID, PROBLEM_ID, 5, 100, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void WhenCreateQuestionForNonExistingProblem_ShouldRespondNotFound() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        createQuestion(new CreateQuestionUseCase.Request(examView.getId(), NONEXISTING_PROBLEM_ID, 5, 100, 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenOneExamAndOneQuestionCreated_WhenDeleteTheQuestion_ShouldSucceed() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        QuestionView question = createQuestionAndGet(new CreateQuestionUseCase.Request(examView.getId(), 2, 5, 100, 1));
        deleteQuestion(examView.getId(), PROBLEM_ID)
                .andExpect(status().isOk());
        assertEquals(getExamOverview(examView.getId()).getQuestions().size(), 0);
    }

    @Test
    void WhenDeleteQuestionWithNonExistingExam_ShouldRespondNotFound() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        QuestionView question = createQuestionAndGet(new CreateQuestionUseCase.Request(examView.getId(), 2, 5, 100, 1));
        deleteQuestion(NONEXISTING_EXAM_ID, PROBLEM_ID)
                .andExpect(status().isNotFound());
    }

    @Test
    void WhenDeleteQuestionWithNonExistingProblem_ShouldRespondNotFound() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        QuestionView question = createQuestionAndGet(new CreateQuestionUseCase.Request(examView.getId(), 2, 5, 100, 1));
        deleteQuestion(examView.getId(), NONEXISTING_PROBLEM_ID)
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenOneExamWithTwoQuestions_WhenGetTheExamOverview_ShouldRespondCorrectly() throws Exception {
        Date current = new Date();
        ExamView examView = createExamAndGet(current, current, "sample-exam");
        createQuestion(new CreateQuestionUseCase.Request(examView.getId(), PROBLEM_ID, 5, 30, 1)).andExpect(status().isOk());
        createQuestion(new CreateQuestionUseCase.Request(examView.getId(), ANOTHER_PROBLEM_ID, 5, 70, 2)).andExpect(status().isOk());

        ExamOverview examOverview = getExamOverview(examView.getId());

        assertEquals(examOverview.getId(), examView.getId());
        assertEquals(examOverview.getName(), "sample-exam");
        assertEquals(examOverview.getStartTime(), current);
        assertEquals(examOverview.getEndTime(), current);
        assertEquals(examOverview.getDescription(), "problem statement");
        assertEquals(examOverview.getQuestions().size(), 2);
        assertEquals(examOverview.getTotalScore(), 100);
        assertEquals(examOverview.getQuestions().get(0).getProblemTitle(), problem.getTitle());
        assertEquals(examOverview.getQuestions().get(1).getProblemTitle(), anotherProblem.getTitle());
    }

    private ExamView createExamAndGet(Date startTime, Date endTime, String name) throws Exception {
        return getBody(createExam(new Exam(name, startTime, endTime, "problem statement"))
                .andExpect(status().isOk()), ExamView.class);
    }

    private ResultActions createExam(Exam exam) throws Exception {
        return mockMvc.perform(post("/api/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new CreateExamUseCase.Request(exam.getName(), exam.getStartTime(), exam.getEndTime(), exam.getDescription()))));
    }

    private ResultActions getExams(int studentId, String type) throws Exception {
        return mockMvc.perform(get("/api/students/{studentId}/exams?type=" + type, studentId));
    }

    private ExamOverview getExamOverview(int examId) throws Exception {
        return getBody(mockMvc.perform(get("/api/exams/{examId}/overview", examId)).andExpect(status().isOk()), ExamOverview.class);
    }

    private void createExamParticipation(ExamParticipation examParticipation) {
        examParticipationRepository.save(examParticipation);
    }

    private ResultActions createQuestion(CreateQuestionUseCase.Request request) throws Exception {
        return mockMvc.perform(post("/api/exams/{examId}/questions", request.getExamId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
    }

    private QuestionView createQuestionAndGet(CreateQuestionUseCase.Request question) throws Exception {
        return getBody(createQuestion(question)
                .andExpect(status().isOk()), QuestionView.class);
    }

    private ResultActions deleteQuestion(int examId, int problemId) throws Exception {
        return mockMvc.perform(delete("/api/exams/{examId}/questions/{problemId}", examId, problemId));
    }


}