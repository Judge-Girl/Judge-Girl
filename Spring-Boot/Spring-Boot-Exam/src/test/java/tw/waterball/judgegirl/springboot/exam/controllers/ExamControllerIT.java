package tw.waterball.judgegirl.springboot.exam.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.ExamParticipation;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.springboot.exam.SpringBootExamApplication;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.JpaExamDataPort;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.JpaExamParticipationDataPort;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.JpaQuestionDataPort;
import tw.waterball.judgegirl.springboot.exam.view.ExamView;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

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
import static tw.waterball.judgegirl.springboot.exam.repositories.jpa.ExamParticipationData.toData;

@ContextConfiguration(classes = SpringBootExamApplication.class)
class ExamControllerIT extends AbstractSpringBootTest {

    @Autowired
    JpaExamDataPort examRepository;
    @Autowired
    JpaExamParticipationDataPort examParticipationRepository;
    @Autowired
    JpaQuestionDataPort questionRepository;

    @Test
    void whenCreateExamWithEndTimeAfterStartTime_shouldSucceed() throws Exception {
        String name = "test-contest";
        Date startTime = new Date(), endTime = new Date();
        Exam exam = new Exam(name, startTime, endTime);

        createExam(exam)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("name").value(name))
                .andExpect(jsonPath("startTime").value(startTime))
                .andExpect(jsonPath("endTime").value(endTime));

    }

    @Test
    void whenCreateExamWithEndTimeBeforeStartTime_shouldRespondBadRequest() throws Exception {
        String name = "test-contest";
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date startTime = cal.getTime(), endTime = new Date();
        Exam exam = new Exam(name, startTime, endTime);

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
    void whenCreateQuestion_shouldSucceed() throws Exception {
        createQuestion(new Question(1, 2, 5, 100))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").exists())
                .andExpect(jsonPath("examId").value(1))
                .andExpect(jsonPath("problemId").value(2))
                .andExpect(jsonPath("quota").value(5))
                .andExpect(jsonPath("score").value(100));
    }

    @Test
    void whenDeleteExistedQuestion_shouldSucceed() throws Exception {
        createQuestion(new Question(1, 2, 5, 100))
                .andExpect(status().isOk());
        deleteQuestion(1, 2)
                .andExpect(status().isOk());
    }

    private ExamView createExamAndGet(Date startTime, Date endTime, String upcoming1) throws Exception {
        return getBody(createExam(new Exam(upcoming1, startTime, endTime))
                .andExpect(status().isOk()), ExamView.class);
    }

    private ResultActions createExam(Exam exam) throws Exception {
        return mockMvc.perform(post("/api/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(exam)));
    }

    private ResultActions getExams(int studentId, String type) throws Exception {
        return mockMvc.perform(get("/api/students/{studentId}/exams?type=" + type, studentId));
    }

    private void createExamParticipation(ExamParticipation examParticipation) {
        examParticipationRepository.save(toData(examParticipation));
    }

    private ResultActions createQuestion(Question question) throws Exception {
        return mockMvc.perform(post("/api/exams/{examId}/questions", question.getExamId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(question)));
    }

    private ResultActions deleteQuestion(int questionId, int examId) throws Exception {
        return mockMvc.perform(delete("/api/exams/{examId}/questions/{questionId}", examId, questionId));
    }

    @AfterEach
    void cleanup() {
        examRepository.deleteAll();
        examParticipationRepository.deleteAll();
        questionRepository.deleteAll();
    }

}