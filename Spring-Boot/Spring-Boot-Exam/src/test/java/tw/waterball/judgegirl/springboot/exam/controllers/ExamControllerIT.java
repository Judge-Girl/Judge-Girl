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

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    void whenGetUpcomingExams_shouldSucceed() throws Exception {
        Date currentTime = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date futureTime = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, -2);
        Date pastTime = cal.getTime();
        createExam(new Exam("upcoming1", futureTime, futureTime)).andExpect(status().isOk());
        createExam(new Exam("upcoming2", futureTime, futureTime)).andExpect(status().isOk());
        createExam(new Exam("upcoming3", futureTime, futureTime)).andExpect(status().isOk());
        createExam(new Exam("past1", pastTime, currentTime)).andExpect(status().isOk());
        createExamParticipation(new ExamParticipation(1, 1));
        createExamParticipation(new ExamParticipation(2, 1));
        createExamParticipation(new ExamParticipation(4, 1));
        createExamParticipation(new ExamParticipation(1, 2));
        createExamParticipation(new ExamParticipation(3, 2));
        createExamParticipation(new ExamParticipation(4, 2));
        String json = getUpcomingExams(1, "upcoming")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Exam> exams = fromJson(json, new TypeReference<List<Exam>>() {
        });
        Set<Integer> examIdSet = new HashSet<>();
        for (Exam exam : exams) {
            examIdSet.add(exam.getId());
        }
        Set<Integer> expectExamIdSet = new HashSet<>(Arrays.asList(1, 2));
        assertEquals(examIdSet, expectExamIdSet);
    }

    private ResultActions createExam(Exam exam) throws Exception {
        return mockMvc.perform(post("/api/exams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(exam)));
    }

    private ResultActions getUpcomingExams(int studentId, String type) throws Exception {
        return mockMvc.perform(get("/api/students/" + Integer.toString(studentId) + "/exams?type=" + type)
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