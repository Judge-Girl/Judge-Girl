package tw.waterball.judgegirl.springboot.academy.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.academy.domain.usecases.homework.CreateHomeworkUseCase;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Group;
import tw.waterball.judgegirl.primitives.exam.MemberId;
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
import tw.waterball.judgegirl.springboot.academy.view.StudentsHomeworkProgress;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.studentapi.clients.FakeStudentServiceDriver;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToSet;
import static tw.waterball.judgegirl.primitives.stubs.SubmissionStubBuilder.submission;
import static tw.waterball.judgegirl.springboot.academy.controllers.ExamControllerTest.LANG_ENV;

@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootAcademyApplication.class)
public class HomeworkControllerTest extends AbstractSpringBootTest {

    public static final int PROBLEM1_ID = 1;
    public static final int PROBLEM2_ID = 2;
    private static final String GROUP_A_NAME = "groupA";
    private static final String GROUP_B_NAME = "groupB";
    private static final String HOMEWORK_NAME = "homeworkName";
    public static final int STUDENT_ID = 11;
    private static final String HOMEWORK_PATH = "/api/homework";
    private static final String HOMEWORK_PROGRESS_PATH = "/api/students/{studentId}/homework/{homeworkId}/progress";
    private static final String GET_STUDENTS_HOMEWORK_PROGRESS_PATH = "/api/students/homework/{homeworkId}/progress";
    private static final String GET_GROUPS_HOMEWORK_PROGRESS_PATH = "/api/groups/homework/{homeworkId}/progress";
    private StudentView studentA;
    private StudentView studentB;
    private StudentView studentC;

    @Autowired
    private FakeProblemServiceDriver problemServiceDriver;

    @Autowired
    private FakeStudentServiceDriver studentServiceDriver;

    @MockBean
    private SubmissionServiceDriver submissionServiceDriver;

    @Autowired
    private HomeworkRepository homeworkRepository;

    @Autowired
    private GroupRepository groupRepository;

    @AfterEach
    void cleanUp() {
        problemServiceDriver.clear();
        homeworkRepository.deleteAll();
        studentServiceDriver.clear();
    }

    @BeforeEach
    void setup() {
        studentA = signUpStudent("studentA", "studentA@example.com", "password");
        studentB = signUpStudent("studentB", "studentB@example.com", "password");
        studentC = signUpStudent("studentC", "studentC@example.com", "password");
    }

    @Test
    public void GivenThreeProblemsCreated_WhenCreateHomeworkThatIncludesManyProblems_ShouldCreateSuccessfully() {
        createProblems(0, 1, 2);

        var homework = createHomeworkAndGet(HOMEWORK_NAME, 0, 1, 2);

        homeworkShouldIncludeProblemIds(homework, 0, 1, 2);
    }

    @DisplayName("Given created two problems [0, 1], " +
            "When create homework to include two problem by ids [0, 1, 2] " +
            "Should respond [0, 1]")
    @Test
    public void testAddProblemsIntoHomeworkByNonExistingProblemIdAndTwoCreatedProblemIds() {
        createProblems(0, 1);

        var homework = createHomeworkAndGet(HOMEWORK_NAME, 0, 1, 2);

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
    }

    @Test
    public void WhenGetHomeworkByNonExistingHomeworkId_ShouldRespondNotFound() throws Exception {
        int nonExistingHomeworkId = 123123;
        getHomework(nonExistingHomeworkId)
                .andExpect(status().isNotFound());
    }

    @Test
    public void GivenHomeworkWithProblems01_WhenAddProblems045IntoHomeWork_HomeworkShouldContainProblems0145() throws Exception {
        createProblems(0, 1, 4, 5);
        var homework = createHomeworkWithProblems(HOMEWORK_NAME, 0, 1);

        addProblemsIntoHomework(homework.id, 1, 4, 5);

        assertTrue(getHomeworkById(homework.id).containProblems(List.of(0, 1, 4, 5)));
    }

    @Test
    public void GivenHomeworkWithProblems013_WhenDeleteHomeworkProblems4230_HomeworkShouldContainProblem1() throws Exception {
        createProblems(0, 1, 3);
        var homework = createHomeworkWithProblems(HOMEWORK_NAME, 0, 1, 3);

        deleteHomeworkProblems(homework.id, 4, 2, 3, 0);

        assertTrue(getHomeworkById(homework.id).containProblems(List.of(1)));
    }

    @DisplayName("Given a homework consists of two problems [1, 2] and the student achieved AC in 1 and CE in 2," +
            "When get the student's homework progress, " +
            "Should respond the two best records [AC, CE] within the homework progress")
    @Test
    public void testGetHomeworkProgress() throws Exception {
        var homework = createHomeworkWithProblems(PROBLEM1_ID, PROBLEM2_ID);
        var bestRecord1 = achieveBestRecord(PROBLEM1_ID, submission("AC").AC(1, 1, 100).build(STUDENT_ID, PROBLEM1_ID, LANG_ENV));
        var bestRecord2 = achieveBestRecord(PROBLEM2_ID, submission("CE").CE(100).build(STUDENT_ID, PROBLEM2_ID, LANG_ENV));

        HomeworkProgress homeworkProgress = getHomeworkProgress(STUDENT_ID, homework.id);

        assertEquals(homework, homeworkProgress.homework);
        homeworkProgressShouldIncludeTwoBestRecords(homework, homeworkProgress, bestRecord1, bestRecord2);
    }

    @DisplayName("Given a homework consists of two problems [p1, p2] and two students [A, B]," +
            "And student A achieved AC in p1 and CE in p2," +
            "And student B achieved AC in p1 and didn't answer p2," +
            "When getting students homework progress," +
            "Then student A should get 100 scores in p1 and 0 scores in p2," +
            "And student B should get 100 scores in p1 and 0 scores in p2,")
    @Test
    public void testGetStudentsHomeworkProgress() throws Exception {
        var homework = createHomeworkWithProblems(PROBLEM1_ID, PROBLEM2_ID);
        achieveBestRecord(PROBLEM1_ID, submission("AC").AC(1, 1, 100).build(studentA.id, PROBLEM1_ID, LANG_ENV));
        achieveBestRecord(PROBLEM2_ID, submission("CE").CE(100).build(studentA.id, PROBLEM2_ID, LANG_ENV));
        achieveBestRecord(PROBLEM1_ID, submission("AC").AC(1, 1, 100).build(studentB.id, PROBLEM1_ID, LANG_ENV));

        var studentsHomeworkProgresses = getStudentsHomeworkProgress(homework.id, studentA.email, studentB.email).getProgress();

        assertTrue(studentsHomeworkProgresses.containsKey(studentA.email));
        assertTrue(studentsHomeworkProgresses.containsKey(studentB.email));
        var progressA = studentsHomeworkProgresses.get(studentA.email);
        var progressB = studentsHomeworkProgresses.get(studentB.email);
        progressShouldHaveGrade(progressA, studentA, homework.problemIds);
        progressShouldHaveGrade(progressB, studentB, homework.problemIds);
    }

    @DisplayName("Given a homework consists of two problems [p1, p2] and three students [A, B, C]," +
            "And student A achieved AC in p1 and CE in p2," +
            "And student B achieved AC in p1 and CE in p2," +
            "And student C achieved CE in p1 and didn't answer p2," +
            "And group students[A,B] to the group A," +
            "And group students[C] to the group B," +
            "When getting the groups homework progress," +
            "Then student A should get 100 scores in p1 and 0 scores in p2," +
            "And student B should get 100 scores in p1 and 0 scores in p2," +
            "And student C should get 0 scores in p1 and 0 scores in p2,")
    @Test
    public void testGetGroupsHomeworkProgress() throws Exception {
        var homework = createHomeworkWithProblems(PROBLEM1_ID, PROBLEM2_ID);
        achieveBestRecord(PROBLEM1_ID, submission("AC").AC(1, 1, 100).build(studentA.id, PROBLEM1_ID, LANG_ENV));
        achieveBestRecord(PROBLEM2_ID, submission("CE").CE(100).build(studentA.id, PROBLEM2_ID, LANG_ENV));
        achieveBestRecord(PROBLEM1_ID, submission("AC").AC(1, 1, 100).build(studentB.id, PROBLEM1_ID, LANG_ENV));
        achieveBestRecord(PROBLEM2_ID, submission("CE").CE(100).build(studentB.id, PROBLEM2_ID, LANG_ENV));
        achieveBestRecord(PROBLEM1_ID, submission("CE").CE(100).build(studentC.id, PROBLEM1_ID, LANG_ENV));
        givenGroupWithMembers(GROUP_A_NAME, studentA.id, studentB.id);
        givenGroupWithMembers(GROUP_B_NAME, studentC.id);

        var groupsHomeworkProgresses = getGroupsHomeworkProgress(homework.id, GROUP_A_NAME, GROUP_B_NAME).progress;

        assertTrue(groupsHomeworkProgresses.containsKey(studentA.email));
        assertTrue(groupsHomeworkProgresses.containsKey(studentB.email));
        assertTrue(groupsHomeworkProgresses.containsKey(studentC.email));
        var progressA = groupsHomeworkProgresses.get(studentA.email);
        var progressB = groupsHomeworkProgresses.get(studentB.email);
        var progressC = groupsHomeworkProgresses.get(studentC.email);
        progressShouldHaveGrade(progressA, studentA, homework.problemIds);
        progressShouldHaveGrade(progressB, studentB, homework.problemIds);
        progressShouldHaveGrade(progressC, studentC, homework.problemIds);
    }

    private void progressShouldHaveGrade(StudentsHomeworkProgress.StudentProgress progress, StudentView student, List<Integer> problemIds) {
        assertEquals(student.id, progress.studentId);
        assertEquals(student.name, progress.studentName);

        var problemScores = progress.problemScores;
        for (int problemId : problemIds) {
            int actualProblemScore = problemScores.get(problemId);
            try {
                var bestRecord = submissionServiceDriver.findBestRecord(problemId, student.id);
                int expectedProblemScore = bestRecord != null ? bestRecord.getVerdict().getTotalGrade() : 0;
                assertEquals(expectedProblemScore, actualProblemScore);
            } catch (NotFoundException e) {
                assertEquals(0, actualProblemScore);
            }
        }
    }

    private HomeworkView createHomeworkWithProblems(String homeworkName, int... problemIds) {
        createProblems(problemIds);
        return createHomeworkAndGet(homeworkName, problemIds);
    }

    private HomeworkView createHomeworkWithProblems(int... problemIds) {
        createProblems(problemIds);
        return createHomeworkAndGet(HOMEWORK_NAME, problemIds);
    }

    private SubmissionView achieveBestRecord(int problemId, Submission submission) {
        int studentId = submission.getStudentId();
        var bestRecord = SubmissionView.toViewModel(submission);
        when(submissionServiceDriver.findBestRecord(problemId, studentId))
                .thenReturn(bestRecord);
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

    private List<HomeworkView> createHomeworkListAndGet(int homeworkCount) {
        return range(0, homeworkCount)
                .mapToObj(i -> createHomeworkAndGet(HOMEWORK_NAME + i))
                .collect(toList());
    }

    @SneakyThrows
    private HomeworkView createHomeworkAndGet(String homeworkName, int... problemIds) {
        return getBody(createHomework(homeworkName, problemIds), HomeworkView.class);
    }

    private ResultActions createHomework(String homeworkName, int... problemIds) throws Exception {
        CreateHomeworkUseCase.Request request = new CreateHomeworkUseCase.Request(homeworkName, stream(problemIds).boxed().collect(toList()));
        return mockMvc.perform(withAdminToken(post(HOMEWORK_PATH))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk());
    }

    private ResultActions addProblemsIntoHomework(int homeworkId, int... problemIds) throws Exception {
        return mockMvc.perform(withAdminToken(
                        post(HOMEWORK_PATH + "/{homeworkId}/problems", homeworkId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(problemIds)))
                .andExpect(status().isOk());
    }

    private ResultActions deleteHomeworkProblems(int homeworkId, int... problemIds) throws Exception {
        return mockMvc.perform(withAdminToken(
                        delete(HOMEWORK_PATH + "/{homeworkId}/problems", homeworkId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(problemIds)))
                .andExpect(status().isOk());
    }

    private ResultActions getHomework(int homeworkId) throws Exception {
        return mockMvc.perform(withAdminToken(
                get(HOMEWORK_PATH + "/{homeworkId}", homeworkId)));
    }

    private HomeworkView getHomeworkById(int homeworkId) throws Exception {
        return getBody(mockMvc.perform(withAdminToken(
                get(HOMEWORK_PATH + "/{homeworkId}", homeworkId))), HomeworkView.class);
    }

    private List<HomeworkView> getAllHomework() throws Exception {
        return getBody(mockMvc.perform(
                        withAdminToken(get(HOMEWORK_PATH)))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private void deleteHomework(int homeworkId) throws Exception {
        mockMvc.perform(withAdminToken(
                        delete(HOMEWORK_PATH + "/{homeworkId}", homeworkId)))
                .andExpect(status().isOk());
    }

    private void createProblems(int... problemIds) {
        stream(problemIds).boxed()
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
        return getBody(mockMvc.perform(
                        withAdminToken(get(HOMEWORK_PROGRESS_PATH, studentId, homeworkId)))
                .andExpect(status().isOk()), HomeworkProgress.class);
    }

    private StudentsHomeworkProgress getStudentsHomeworkProgress(int homeworkId, String... studentEmails) throws Exception {
        return getBody(mockMvc.perform(
                        withAdminToken(post(GET_STUDENTS_HOMEWORK_PROGRESS_PATH, homeworkId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(studentEmails)))
                .andExpect(status().isOk()), StudentsHomeworkProgress.class);
    }


    private StudentsHomeworkProgress getGroupsHomeworkProgress(int homeworkId, String... groupNames) throws Exception {
        return getBody(mockMvc.perform(
                        withAdminToken(post(GET_GROUPS_HOMEWORK_PROGRESS_PATH, homeworkId))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(toJson(groupNames)))
                .andExpect(status().isOk()), StudentsHomeworkProgress.class);
    }

    private StudentView signUpStudent(String name, String email, String password) {
        Student newStudent = new Student(name, email, password);
        studentServiceDriver.addStudent(newStudent);
        return StudentView.toViewModel(newStudent);
    }

    private Group givenGroupWithMembers(String name, Integer... studentIds) {
        Group group = groupRepository.save(new Group(name));
        group.addMembers(mapToSet(studentIds, MemberId::new));
        return groupRepository.save(group);
    }
}