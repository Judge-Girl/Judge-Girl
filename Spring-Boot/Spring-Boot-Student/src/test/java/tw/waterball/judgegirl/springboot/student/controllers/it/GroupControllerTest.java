package tw.waterball.judgegirl.springboot.student.controllers.it;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.SpringBootStudentApplication;
import tw.waterball.judgegirl.springboot.student.view.GroupView;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;
import tw.waterball.judgegirl.studentservice.domain.usecases.CreateGroupUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author - wally55077@gmail.com
 */
@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootStudentApplication.class)
public class GroupControllerTest extends AbstractSpringBootTest {

    private static final String GROUP_NAME = "groupName";
    private static final String STUDENT_PATH = "/api/students";
    private static final String GROUP_PATH = "/api/groups";

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private GroupRepository groupRepository;

    @AfterEach
    void cleanUp() {
        groupRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @Test
    public void WhenCreateGroupWithUniqueName_ShouldCreateSuccessfully() throws Exception {
        GroupView group = getBody(createGroup(GROUP_NAME)
                .andExpect(status().isOk()), GroupView.class);

        assertEquals(GROUP_NAME, group.name);
        assertTrue(groupRepository.existsByName(GROUP_NAME));
    }

    @Test
    public void GivenOneGroupCreated_WhenCreateGroupWithDuplicateName_ShouldRejectWithBadRequest() throws Exception {
        createGroup(GROUP_NAME);

        createGroup(GROUP_NAME).andExpect(status().isBadRequest());
    }

    @Test
    public void GivenOneGroupCreated_WhenGetGroupById_ShouldRespondGroup() throws Exception {
        GroupView group = createGroupAndGet(GROUP_NAME);

        GroupView body = getBody(getGroupById(group.id).andExpect(status().isOk()), GroupView.class);

        assertEquals(group, body);
    }

    @Test
    public void GivenTwoGroupsCreated_WhenGetAll_ShouldRespondTwoGroups() throws Exception {
        createGroup(GROUP_NAME + 1);
        createGroup(GROUP_NAME + 2);

        List<GroupView> body = getBody(getAllGroups().andExpect(status().isOk()), new TypeReference<>() {
        });

        assertEquals(2, body.size());
    }

    private ResultActions getAllGroups() throws Exception {
        return mockMvc.perform(get(GROUP_PATH));
    }

    private ResultActions createGroup(String groupName) throws Exception {
        CreateGroupUseCase.Request request = new CreateGroupUseCase.Request(groupName);
        return mockMvc.perform(post(GROUP_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
    }

    @Test
    public void WhenGetGroupByNonExistingGroupId_ShouldRespondNotFound() throws Exception {
        int nonExistingGroupId = 123123;
        getGroupById(nonExistingGroupId).andExpect(status().isNotFound());
    }

    @Test
    public void GivenOneGroupCreated_WhenDeleteGroupById_ShouldDeleteSuccessfully() throws Exception {
        GroupView group = createGroupAndGet(GROUP_NAME);

        int groupId = group.id;
        deleteGroupById(groupId).andExpect(status().isOk());

        getGroupById(groupId).andExpect(status().isNotFound());
    }

    private GroupView createGroupAndGet(String groupName) throws Exception {
        return getBody(createGroup(groupName), GroupView.class);
    }

    private ResultActions getGroupById(Integer id) throws Exception {
        return mockMvc.perform(get(GROUP_PATH + "/{groupId}", id));
    }

    @Test
    public void WhenDeleteGroupByNonExistingGroupId_ShouldRespondNotFound() throws Exception {
        int nonExistingGroupId = 123123;
        deleteGroupById(nonExistingGroupId)
                .andExpect(status().isNotFound());
    }

    private ResultActions deleteGroupById(Integer id) throws Exception {
        return mockMvc.perform(delete(GROUP_PATH + "/{groupId}", id));
    }

    @Test
    @Transactional
    public void GivenOneGroupCreated_WhenAddTwoStudentsIntoTheGroup_ShouldAddSuccessfully() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        StudentView studentViewA = signUpAndGetStudent("A");
        StudentView studentViewB = signUpAndGetStudent("B");

        addStudentIntoGroup(groupId, studentViewA.id);
        addStudentIntoGroup(groupId, studentViewB.id);

        Group group = groupRepository.findGroupById(groupId).orElseThrow(NotFoundException::new);
        assertEquals(2, group.getStudents().size());
        Student studentA = studentRepository.findStudentById(studentViewA.id).orElseThrow(NotFoundException::new);
        assertEquals(1, studentA.getGroups().size());
        Student studentB = studentRepository.findStudentById(studentViewB.id).orElseThrow(NotFoundException::new);
        assertEquals(1, studentB.getGroups().size());
    }

    @Test
    public void GivenOneStudentAddedIntoCreatedGroup_WhenAddStudentMultipleTimesIntoTheGroup_ShouldRespondOk() throws Exception {
        GroupView body = createGroupAndGet(GROUP_NAME);
        StudentView studentA = signUpAndGetStudent("A");
        int groupId = body.id;
        addStudentIntoGroup(groupId, studentA.id);
        addStudentIntoGroup(groupId, studentA.id)
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void GivenTwoStudentsAddedIntoCreatedGroup_WhenDeleteOneStudentFromTheGroup_ThenGroupShouldHaveOneStudent() throws Exception {
        GroupView body = createGroupAndGet(GROUP_NAME);
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");
        int groupId = body.id;
        addStudentIntoGroup(groupId, studentA.id);
        addStudentIntoGroup(groupId, studentB.id);

        deleteStudentFromGroup(groupId, studentA.id);

        Group group = groupRepository.findGroupById(groupId).orElseThrow(NotFoundException::new);
        Set<Student> students = group.getStudents();
        assertEquals(1, students.size());
        assertEquals(studentB.id, students.stream().findFirst().orElseThrow(NotFoundException::new).getId());
    }

    @Test
    @Transactional
    public void GivenTwoStudentsAddedIntoCreatedGroup_WhenDeleteGroupById_ShouldDeleteSuccessfully() throws Exception {
        GroupView body = createGroupAndGet(GROUP_NAME);
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");
        int groupId = body.id;
        addStudentIntoGroup(groupId, studentA.id);
        addStudentIntoGroup(groupId, studentB.id);

        deleteGroupById(groupId);

        anotherTransaction(() -> {
            Student student = studentRepository.findStudentById(studentA.id).orElseThrow(NotFoundException::new);
            assertEquals(0, student.getGroups().size());
        });
    }

    private StudentView signUpAndGetStudent(String name) throws Exception {
        String email = name + "@gmail.com";
        String password = "password" + name;
        return getBody(signUp(name, email, password), StudentView.class);
    }

    @Test
    public void GivenTwoStudentsAddedIntoCreatedGroup_WhenGetStudentsByGroupId_ShouldRespondTwoStudents() throws Exception {
        GroupView group = createGroupAndGet(GROUP_NAME);
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");
        int groupId = group.id;
        addStudentIntoGroup(groupId, studentA.id);
        addStudentIntoGroup(groupId, studentB.id);

        List<StudentView> respondedStudents = getBody(getStudentsByGroupId(groupId)
                .andExpect(status().isOk()), new TypeReference<>() {
        });

        assertEquals(2, respondedStudents.size());
    }

    @Test
    public void GivenOneStudentAddedIntoTwoCreatedGroups_WhenGetGroupsByStudentId_ShouldRespondTwoGroups() throws Exception {
        GroupView groupA = createGroupAndGet(GROUP_NAME + "A");
        GroupView groupB = createGroupAndGet(GROUP_NAME + "B");
        StudentView studentA = signUpAndGetStudent("A");
        int studentId = studentA.id;
        addStudentIntoGroup(groupA.id, studentId);
        addStudentIntoGroup(groupB.id, studentId);

        List<GroupView> respondedGroups = getBody(getGroupsByStudentId(studentId)
                .andExpect(status().isOk()), new TypeReference<>() {
        });

        assertEquals(2, respondedGroups.size());
    }

    private ResultActions getGroupsByStudentId(int studentId) throws Exception {
        return mockMvc.perform(get("/api/students/{studentId}/groups", studentId));
    }

    private ResultActions signUp(String name, String email, String password) throws Exception {
        Student newStudent = new Student(name, email, password);
        return mockMvc.perform(post(STUDENT_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(newStudent)));
    }

    private ResultActions addStudentIntoGroup(int groupId, int studentId) throws Exception {
        return mockMvc.perform(post(GROUP_PATH + "/{groupId}/students/{studentId}", groupId, studentId));
    }

    private ResultActions deleteStudentFromGroup(int groupId, int studentId) throws Exception {
        return mockMvc.perform(delete(GROUP_PATH + "/{groupId}/students/{studentId}", groupId, studentId));
    }

    private ResultActions getStudentsByGroupId(int groupId) throws Exception {
        return mockMvc.perform(get(GROUP_PATH + "/{groupId}/students", groupId));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void GivenOneGroupCreated_WhenAddTwoStudentsIntoTheGroupByMailList_ShouldRespondEmptyErrorListAndGroupShouldHaveTwoStudents() throws Exception {
        GroupView body = createGroupAndGet(GROUP_NAME);
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");

        int groupId = body.id;
        String[] mailList = {studentA.email, studentB.email};
        List<String> errorList = (List<String>) getBody(addStudentsIntoGroupByMailList(groupId, mailList)
                .andExpect(status().isOk()), Map.class).get("errorList");

        assertTrue(errorList.isEmpty());
        Group group = groupRepository.findGroupById(groupId).orElseThrow(NotFoundException::new);
        assertEquals(2, group.getStudents().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void GivenTwoStudentsAddedIntoCreatedGroup_WhenAddSameStudentsMultipleTimesIntoTheGroupByMailList_ShouldRespondEmptyErrorListAndGroupShouldHaveTwoStudents() throws Exception {
        GroupView body = createGroupAndGet(GROUP_NAME);
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");
        int groupId = body.id;
        addStudentIntoGroup(groupId, studentA.id);
        addStudentIntoGroup(groupId, studentB.id);

        String[] mailList = {studentA.email, studentB.email};
        List<String> errorList = (List<String>) getBody(addStudentsIntoGroupByMailList(groupId, mailList)
                .andExpect(status().isOk()), Map.class).get("errorList");

        assertTrue(errorList.isEmpty());
        Group group = groupRepository.findGroupById(groupId).orElseThrow(NotFoundException::new);
        assertEquals(2, group.getStudents().size());
    }

    @SuppressWarnings("unchecked")
    @DisplayName("Given student A(A@gmail.com) and a Group, " +
            "When add students by mail-list (A@gmail.com, B@gmail.com), " +
            "Should return an errorList with [B@gmail.com]")
    @Test
    public void testAddStudentsIntoGroupByNonExistingEmails() throws Exception {
        GroupView body = createGroupAndGet(GROUP_NAME);
        StudentView studentA = signUpAndGetStudent("A");

        String nonExistingStudentEmail = "B@gmail.com";
        String[] mailList = {studentA.email, nonExistingStudentEmail};
        int groupId = body.id;
        List<String> errorList = (List<String>) getBody(addStudentsIntoGroupByMailList(groupId, mailList)
                .andExpect(status().isOk()), Map.class).get("errorList");

        assertEquals(nonExistingStudentEmail, errorList.get(0));
        Group group = groupRepository.findGroupById(groupId).orElseThrow(NotFoundException::new);
        assertEquals(1, group.getStudents().size());
    }

    private ResultActions addStudentsIntoGroupByMailList(int groupId, String[] mailList) throws Exception {
        return mockMvc.perform(post(GROUP_PATH + "/{groupId}/students", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mailList)));
    }

}
