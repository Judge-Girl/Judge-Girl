package tw.waterball.judgegirl.springboot.exam.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.springboot.exam.Exam;
import tw.waterball.judgegirl.springboot.exam.ExamParticipation;
import tw.waterball.judgegirl.springboot.exam.SpringBootExamApplication;
import tw.waterball.judgegirl.springboot.exam.repositories.ExamParticipationRepository;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.commons.utils.DateUtils.afterCurrentTime;
import static tw.waterball.judgegirl.commons.utils.DateUtils.beforeCurrentTime;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

@ContextConfiguration(classes = SpringBootExamApplication.class)
class ExamControllerIT extends AbstractSpringBootTest {

    @Autowired
    ExamParticipationRepository examParticipationRepository;

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
        Exam upcoming1 = createExamAndGet(future, future, "upcoming1");
        Exam upcoming2 = createExamAndGet(future, future, "upcoming2");
        Exam past1 = createExamAndGet(past, now, "past1");
        createExamParticipation(new ExamParticipation(upcoming1.getId(), 1));
        createExamParticipation(new ExamParticipation(upcoming2.getId(), 1));
        createExamParticipation(new ExamParticipation(past1.getId(), 1));

        List<Exam> exams = getBody(
                getExams(1, "upcoming")
                        .andExpect(status().isOk()), new TypeReference<>() {
                });

        List<Integer> actualExamIdSet = mapToList(exams, Exam::getId);
        List<Integer> expectExamIdSet = asList(upcoming1.getId(), upcoming2.getId());
        assertEqualsIgnoreOrder(expectExamIdSet, actualExamIdSet);
    }

    private Exam createExamAndGet(Date startTime, Date endTime, String upcoming1) throws Exception {
        return getBody(createExam(new Exam(upcoming1, startTime, endTime))
                .andExpect(status().isOk()), Exam.class);
    }

    private ResultActions createExam(Exam exam) throws Exception {
        return mockMvc.perform(post("/api/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(exam)));
    }

    private ResultActions getExams(int studentId, String type) throws Exception {
        return mockMvc.perform(get("/api/students/{studentId}/exams?type=" + type, studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(studentId)));
    }

    private void createExamParticipation(ExamParticipation examParticipation) {
        examParticipationRepository.save(examParticipation);
    }

    @AfterEach
    void cleanup() {
        examParticipationRepository.deleteAll();
    }

}