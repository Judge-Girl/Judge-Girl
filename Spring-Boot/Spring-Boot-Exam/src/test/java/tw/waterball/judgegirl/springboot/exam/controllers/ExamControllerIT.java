package tw.waterball.judgegirl.springboot.exam.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.ExamParticipation;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.ExamParticipationRepository;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;
import tw.waterball.judgegirl.examservice.repositories.QuestionRepository;
import tw.waterball.judgegirl.examservice.usecases.CreateExamUseCase;
import tw.waterball.judgegirl.problemapi.clients.FakeProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.exam.SpringBootExamApplication;
import tw.waterball.judgegirl.springboot.exam.view.ExamOverview;
import tw.waterball.judgegirl.springboot.exam.view.ExamView;
import tw.waterball.judgegirl.springboot.exam.view.QuestionView;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.commons.utils.DateUtils.afterCurrentTime;
import static tw.waterball.judgegirl.commons.utils.DateUtils.beforeCurrentTime;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

@Transactional
@ContextConfiguration(classes = SpringBootExamApplication.class)
class ExamControllerIT extends AbstractSpringBootTest {

    @Autowired
    ExamRepository examRepository;
    @Autowired
    ExamParticipationRepository examParticipationRepository;
    @Autowired
    QuestionRepository questionRepository;
    @Autowired
    FakeProblemServiceDriver problemServiceDriver;

    @Configuration
    public static class TestConfig {
        @Bean
        @Primary
        public FakeProblemServiceDriver fakeProblemServiceDriver() {
            return new FakeProblemServiceDriver();
        }
    }

    @Test
    void whenCreateExamWithEndTimeAfterStartTime_shouldSucceed() throws Exception {
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
    void whenCreateExamWithEndTimeBeforeStartTime_shouldRespondBadRequest() throws Exception {
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
    void GivenStudentParticipatesOnThreeExams_AndTwoOfThemAreUpcoming_WhenGetUpcomingExams_shouldResponseThoseTwo() throws Exception {
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
    void givenOneExam_whenCreateQuestionForExistedExam_shouldSucceed() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        createQuestion(new Question(examView.getId(), 2, 5, 100, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("examId").value(1))
                .andExpect(jsonPath("problemId").value(2))
                .andExpect(jsonPath("quota").value(5))
                .andExpect(jsonPath("score").value(100));
    }

    @Test
    void whenCreateQuestionForNonExistedExam_shouldBadRequest() throws Exception {
        createQuestion(new Question(1, 2, 5, 100, 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenCreateQuestionForNonExistedProblem_shouldBadRequest() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        createQuestion(new Question(examView.getId(), 1, 5, 100, 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenOneExamAndOneQuestionCreated_whenDeleteTheQuestion_shouldSucceed() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        QuestionView question = createQuestionAndGet(new Question(examView.getId(), 2, 5, 100, 1));
        deleteQuestion(question.getId(), examView.getId())
                .andExpect(status().isOk());
    }

    @Test
    void whenDeleteNonExistedQuestion_shouldRespondBadRequest() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        QuestionView question = createQuestionAndGet(new Question(examView.getId(), 2, 5, 100, 1));
        deleteQuestion(question.getId(), examView.getId() + 1)
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetExistedExamOverview_shouldSucceed() throws Exception {
        Date current = new Date();
        ExamView examView = createExamAndGet(current, current, "sample-exam");
        createQuestion(new Question(examView.getId(), 2, 5, 30, 1)).andExpect(status().isOk());
        createQuestion(new Question(examView.getId(), 2, 5, 70, 2)).andExpect(status().isOk());
        ExamOverview examOverview = getExamOverview(examView.getId());
        Assertions.assertEquals(examOverview.getName(), "sample-exam");
        Assertions.assertEquals(examOverview.getStartTime(), current);
        Assertions.assertEquals(examOverview.getEndTime(), current);
        Assertions.assertEquals(examOverview.getDescription(), "problem statement");
        Assertions.assertEquals(examOverview.getQuestionViews().size(), 2);
        Assertions.assertEquals(examOverview.getTotalScore(), 100);
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

    private ResultActions createQuestion(Question question) throws Exception {
        return mockMvc.perform(post("/api/exams/{examId}/questions", question.getExamId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(question)));
    }

    private QuestionView createQuestionAndGet(Question question) throws Exception {
        return getBody(createQuestion(question)
                .andExpect(status().isOk()), QuestionView.class);
    }

    private ResultActions deleteQuestion(int questionId, int examId) throws Exception {
        return mockMvc.perform(delete("/api/exams/{examId}/questions/{questionId}", examId, questionId));
    }

    @BeforeEach
    void setup() {
        ProblemView problemView = new ProblemView();
        problemView.setId(2);
        problemServiceDriver.addProblemView(problemView);
    }


    @AfterEach
    void cleanup() {
        examRepository.deleteAll();
        examParticipationRepository.deleteAll();
        questionRepository.deleteAll();
    }

}