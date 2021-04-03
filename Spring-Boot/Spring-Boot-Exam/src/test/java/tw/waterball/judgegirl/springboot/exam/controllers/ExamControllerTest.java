package tw.waterball.judgegirl.springboot.exam.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.ExamFilter;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.commons.utils.DateUtils.afterCurrentTime;
import static tw.waterball.judgegirl.commons.utils.DateUtils.beforeCurrentTime;

@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootExamApplication.class)
class ExamControllerTest extends AbstractSpringBootTest {
    public static final int PROBLEM_ID = 2;
    public static final int ANOTHER_PROBLEM_ID = 300;

    public static final int NONEXISTING_EXAM_ID = 9999;
    public static final int NONEXISTING_PROBLEM_ID = 9999;
    public static final int STUDENT_ID = 1;

    @Autowired
    ExamRepository examRepository;
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

    @DisplayName("Given Student participates Exams A, B, C, D (only B, D are upcoming) " +
            "When get student's upcoming exams, Should respond B D")
    @Test
    void testFilterUpcomingStudentExams() throws Exception {
        givenStudentParticipatingExams(STUDENT_ID,
                givenCurrentExams("A", "C"),
                givenUpcomingExams("B", "D"));

        List<ExamView> exams = getStudentExams(STUDENT_ID, ExamFilter.Status.upcoming);
        shouldRespondExams(exams, "B", "D");
    }

    @DisplayName("Given Student participates Exams A, B, C, D, E, F, G (only B, F, G are past) " +
            "When get student's past exams with skip=1, size=2, Should respond F, G")
    @Test
    void testFilterPastStudentExamsWithPaging() throws Exception {
        givenStudentParticipatingExams(STUDENT_ID,
                givenCurrentExams("A", "C", "D", "E"),
                givenPastExams("B", "F", "G"));

        List<ExamView> exams = getStudentExams(STUDENT_ID, ExamFilter.Status.past, 1, 2);
        shouldRespondExams(exams, "F", "G");
    }


    @DisplayName("Given Student participates Exams A, B, C, D, E, F, G, " +
            "When get all student's exams with skip=1, size=4, Should respond B, C, D, E, F")
    @Test
    void testGetALlStudentsExams() throws Exception {
        givenStudentParticipatingExams(
                STUDENT_ID,
                givenCurrentExams("A", "B", "C"),
                givenUpcomingExams("D", "E"),
                givenPastExams("F", "G"));

        List<ExamView> exams = getStudentExams(STUDENT_ID, ExamFilter.Status.all, 1, 5);
        shouldRespondExams(exams, "B", "C", "D", "E", "F");
    }


    @DisplayName("Given Student participates Exams A, B, C, D, E, F, G (only A, B, C, D are current) " +
            "When get student's current exams with skip=1, size=500000, Should respond B, C, D")
    @Test
    void testFilterCurrentStudentExamsWithPaging() throws Exception {
        givenStudentParticipatingExams(STUDENT_ID,
                givenPastExams("E", "F", "G"),
                givenCurrentExams("A", "B", "C", "D"));

        List<ExamView> exams = getStudentExams(STUDENT_ID, ExamFilter.Status.current, 1, 50000);
        shouldRespondExams(exams, "B", "C", "D");
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
        createQuestion(new CreateQuestionUseCase.Request(examView.getId(), 2, 5, 100, 1));
        deleteQuestion(examView.getId(), PROBLEM_ID)
                .andExpect(status().isOk());
        assertEquals(getExamOverview(examView.getId()).getQuestions().size(), 0);
    }

    @Test
    void WhenDeleteQuestionWithNonExistingExam_ShouldRespondNotFound() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        createQuestion(new CreateQuestionUseCase.Request(examView.getId(), 2, 5, 100, 1));
        deleteQuestion(NONEXISTING_EXAM_ID, PROBLEM_ID)
                .andExpect(status().isNotFound());
    }

    @Test
    void WhenDeleteQuestionWithNonExistingProblem_ShouldRespondNotFound() throws Exception {
        ExamView examView = createExamAndGet(new Date(), new Date(), "sample-exam");
        createQuestion(new CreateQuestionUseCase.Request(examView.getId(), 2, 5, 100, 1));
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

    @Test
    void GivenExams_A_B_WhenGetAllExams_ShouldRespondExams_A_B() throws Exception {
        givenCurrentExams("A", "B");

        List<ExamView> exams = getAllExams();
        shouldRespondExams(exams, "A", "B");
    }

    @Test
    void GivenExams_A_B_C_D_E_WhenGetExamsWithSkip2Size2_ShouldRespondExams_C_D() throws Exception {
        givenCurrentExams("A", "B", "C", "D", "E");

        List<ExamView> exams = getExamsWithPaging(2, 2);
        shouldRespondExams(exams, "C", "D");
    }

    @Test
    void GivenExams_A_B_C_D_E_WhenGetExamsWithSkip2Size1000_ShouldRespondExams_C_D_E() throws Exception {
        givenCurrentExams("A", "B", "C", "D", "E");

        List<ExamView> exams = getExamsWithPaging(2, 1000);
        shouldRespondExams(exams, "C", "D", "E");
    }


    @DisplayName("Given exams A, B, C, D, E (A, C, D are upcoming), " +
            "When filter upcoming exams with skip=1, size=1, " +
            "Then should respond only C")
    @Test
    void testFilterUpcomingExams() throws Exception {
        givenCurrentExams("B", "E");
        givenUpcomingExams("A", "C", "D");

        List<ExamView> exams = getExamsWithPaging(ExamFilter.Status.upcoming, 1, 1);
        shouldRespondExams(exams, "C");
    }

    @DisplayName("Given exams A, B, C, D, E, F, G, H, I (A, B, G, H, I are past), " +
            "When filter past exams with skip=2, size=3, " +
            "Then should respond G, H, I")
    @Test
    void testFilterPastExams() throws Exception {
        givenCurrentExams("C", "D", "E", "F");
        givenPastExams("A", "B", "G", "H", "I");

        List<ExamView> exams = getExamsWithPaging(ExamFilter.Status.past, 2, 3);
        shouldRespondExams(exams, "G", "H", "I");
    }

    @DisplayName("Given exams A, B, C, D, E, F, G, H, I (C, D, E, F are current), " +
            "When filter current exams with skip=2, size=9999, " +
            "Then should respond E, F")
    @Test
    void testFilterCurrentExams() throws Exception {
        givenPastExams("A", "B", "G", "H", "I");
        givenCurrentExams("C", "D", "E", "F");

        List<ExamView> exams = getExamsWithPaging(ExamFilter.Status.current, 2, 9999);
        shouldRespondExams(exams, "E", "F");
    }

    private void shouldRespondExams(List<ExamView> exams, String... names) {
        assertEquals(names.length, exams.size());
        for (int i = 0; i < exams.size(); i++) {
            assertEquals(names[i], exams.get(i).name);
        }
    }

    private List<ExamView> getExamsWithPaging(ExamFilter.Status status, int skip, int size) throws Exception {
        return getBody(mockMvc.perform(get("/api/exams?status={status}&&skip={skip}&&size={size}", status, skip, size)), new TypeReference<>() {
        });
    }

    private List<ExamView> getExamsWithPaging(int skip, int size) throws Exception {
        return getBody(mockMvc.perform(get("/api/exams?skip={skip}&&size={size}", skip, size)), new TypeReference<>() {
        });
    }

    private List<ExamView> getAllExams() throws Exception {
        return getBody(mockMvc.perform(get("/api/exams")), new TypeReference<>() {
        });
    }

    @SafeVarargs
    private void givenStudentParticipatingExams(int studentId, List<ExamView>... exams) {
        List<ExamView> allExams = stream(exams).flatMap(List::stream).collect(toList());
        for (ExamView exam : allExams) {
            createExamParticipation(studentId, exam.id);
        }
    }

    private List<ExamView> givenPastExams(String... names) throws Exception {
        return givenExams(beforeCurrentTime(2, HOURS),
                beforeCurrentTime(1, HOURS), names);
    }

    private List<ExamView> givenCurrentExams(String... names) throws Exception {
        return givenExams(new Date(), afterCurrentTime(1, HOURS), names);
    }

    private List<ExamView> givenUpcomingExams(String... names) throws Exception {
        return givenExams(afterCurrentTime(1, HOURS),
                afterCurrentTime(2, HOURS), names);
    }

    private List<ExamView> givenExams(Date startTime, Date endTime, String... names) throws Exception {
        List<ExamView> exams = new ArrayList<>(names.length);
        for (String name : names) {
            exams.add(createExamAndGet(startTime, endTime, name));
        }
        return exams;
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


    private List<ExamView> getStudentExams(int studentId, ExamFilter.Status status, int skip, int size) throws Exception {
        return getBody(mockMvc.perform(get("/api/students/{studentId}/exams?status={status}&&skip={skip}&&size={size}", studentId, status, skip, size))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private List<ExamView> getStudentExams(int studentId, ExamFilter.Status status) throws Exception {
        return getBody(mockMvc.perform(get("/api/students/{studentId}/exams?status=" + status, studentId))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private ExamOverview getExamOverview(int examId) throws Exception {
        return getBody(mockMvc.perform(get("/api/exams/{examId}/overview", examId)).andExpect(status().isOk()), ExamOverview.class);
    }

    private void createExamParticipation(int studentId, int examId) {
        examRepository.addParticipation(examId, studentId);
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