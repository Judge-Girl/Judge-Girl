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
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.SpringBootStudentApplication;
import tw.waterball.judgegirl.studentapi.clients.view.GroupView;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;
import tw.waterball.judgegirl.studentservice.domain.usecases.group.CreateGroupUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        getGroupById(nonExistingGroupId)
                .andExpect(status().isNotFound());
    }

    @Test
    public void GivenOneGroupCreated_WhenDeleteGroupById_ShouldDeleteSuccessfully() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;

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
    public void GivenOneGroupCreated_WhenAddTwoGroupMembers_ShouldAddSuccessfully() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        StudentView studentViewA = signUpAndGetStudent("A");
        StudentView studentViewB = signUpAndGetStudent("B");

        addGroupMember(groupId, studentViewA.id);
        addGroupMember(groupId, studentViewB.id);

        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        assertEquals(2, group.getStudents().size());
        Student studentA = studentRepository.findStudentById(studentViewA.id).orElseThrow();
        assertEquals(1, studentA.getGroups().size());
        Student studentB = studentRepository.findStudentById(studentViewB.id).orElseThrow();
        assertEquals(1, studentB.getGroups().size());
    }

    @Test
    public void GivenOneGroupMemberAddedIntoCreatedGroup_WhenAddGroupMemberMultipleTimesIntoTheGroup_ShouldRespondOk() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        StudentView studentA = signUpAndGetStudent("A");
        addGroupMember(groupId, studentA.id);

        addGroupMember(groupId, studentA.id)
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void GivenTwoGroupMembersAddedIntoCreatedGroup_WhenDeleteOneGroupMemberFromTheGroup_ThenGroupShouldHaveOneGroupMember() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");
        addGroupMember(groupId, studentA.id);
        addGroupMember(groupId, studentB.id);

        deleteGroupMember(groupId, studentA.id);

        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        Set<Student> students = group.getStudents();
        assertEquals(1, students.size());
        assertEquals(studentB.id, students.stream().findFirst().orElseThrow().getId());
    }

    @Test
    @Transactional
    public void GivenTwoGroupMembersAddedIntoCreatedGroup_WhenDeleteGroupById_ShouldDeleteSuccessfully() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");
        addGroupMember(groupId, studentA.id);
        addGroupMember(groupId, studentB.id);

        deleteGroupById(groupId);

        anotherTransaction(() -> {
            Student student = studentRepository.findStudentById(studentA.id).orElseThrow();
            assertEquals(0, student.getGroups().size());
        });
    }

    private StudentView signUpAndGetStudent(String name) throws Exception {
        String email = name + "@gmail.com";
        String password = "password" + name;
        return getBody(signUp(name, email, password), StudentView.class);
    }

    @Test
    public void GivenTwoGroupMembersAddedIntoCreatedGroup_WhenGetGroupMembersByGroupId_ShouldRespondTwoGroupMembers() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");
        addGroupMember(groupId, studentA.id);
        addGroupMember(groupId, studentB.id);

        List<StudentView> respondedGroupMembers = getBody(getGroupMembersByGroupId(groupId)
                .andExpect(status().isOk()), new TypeReference<>() {
        });

        assertEquals(2, respondedGroupMembers.size());
    }

    @Test
    public void GivenOneGroupMemberAddedIntoTwoCreatedGroups_WhenGetGroupsByStudentId_ShouldRespondTwoGroups() throws Exception {
        int groupAId = createGroupAndGet(GROUP_NAME + "A").id;
        int groupBId = createGroupAndGet(GROUP_NAME + "B").id;
        StudentView studentA = signUpAndGetStudent("A");
        int studentId = studentA.id;
        addGroupMember(groupAId, studentId);
        addGroupMember(groupBId, studentId);

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

    private ResultActions addGroupMember(int groupId, int studentId) throws Exception {
        return mockMvc.perform(post(GROUP_PATH + "/{groupId}/students/{studentId}", groupId, studentId));
    }

    private ResultActions deleteGroupMember(int groupId, int studentId) throws Exception {
        return mockMvc.perform(delete(GROUP_PATH + "/{groupId}/students/{studentId}", groupId, studentId));
    }

    private ResultActions getGroupMembersByGroupId(int groupId) throws Exception {
        return mockMvc.perform(get(GROUP_PATH + "/{groupId}/students", groupId));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void GivenOneGroupCreated_WhenAddTwoGroupMembersIntoTheGroupByMailList_ShouldRespondEmptyErrorListAndGroupShouldHaveTwoGroupMembers() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");

        String[] mailList = {studentA.email, studentB.email};
        List<String> errorList = (List<String>) getBody(addGroupMembersIntoGroupByMailList(groupId, mailList)
                .andExpect(status().isOk()), Map.class).get("errorList");

        assertTrue(errorList.isEmpty());
        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        assertEquals(2, group.getStudents().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void GivenTwoGroupMembersAddedIntoCreatedGroup_WhenAddSameGroupMembersMultipleTimesIntoTheGroupByMailList_ShouldRespondEmptyErrorListAndGroupShouldHaveTwoGroupMembers() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        StudentView studentA = signUpAndGetStudent("A");
        StudentView studentB = signUpAndGetStudent("B");
        addGroupMember(groupId, studentA.id);
        addGroupMember(groupId, studentB.id);

        String[] mailList = {studentA.email, studentB.email};
        List<String> errorList = (List<String>) getBody(addGroupMembersIntoGroupByMailList(groupId, mailList)
                .andExpect(status().isOk()), Map.class).get("errorList");

        assertTrue(errorList.isEmpty());
        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        assertEquals(2, group.getStudents().size());
    }

    @SuppressWarnings("unchecked")
    @DisplayName("Given group member A(A@gmail.com) and a Group, " +
            "When add group members by mail-list (A@gmail.com, B@gmail.com), " +
            "Should return an errorList with [B@gmail.com]")
    @Test
    public void testAddGroupMembersIntoGroupByNonExistingEmails() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        StudentView studentA = signUpAndGetStudent("A");

        String nonExistingStudentEmail = "B@gmail.com";
        String[] mailList = {studentA.email, nonExistingStudentEmail};
        List<String> errorList = (List<String>) getBody(addGroupMembersIntoGroupByMailList(groupId, mailList)
                .andExpect(status().isOk()), Map.class).get("errorList");

        assertEquals(nonExistingStudentEmail, errorList.get(0));
        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        assertEquals(1, group.getStudents().size());
    }

    private ResultActions addGroupMembersIntoGroupByMailList(int groupId, String[] mailList) throws Exception {
        return mockMvc.perform(post(GROUP_PATH + "/{groupId}/students", groupId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mailList)));
    }

    @Test
    public void GivenGroupMembers_A_B_C_AddedIntoGroup_WhenDeleteStudentsByIds_A_B_ShouldRemainGroupMemberCInGroup() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        int studentAId = signUpAndGetStudent("A").id;
        int studentBId = signUpAndGetStudent("B").id;
        int studentCId = signUpAndGetStudent("C").id;
        addGroupMember(groupId, studentAId);
        addGroupMember(groupId, studentBId);
        addGroupMember(groupId, studentCId);

        deleteGroupMembersByIds(groupId, studentAId, studentBId)
                .andExpect(status().isOk());

        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        Set<Student> students = group.getStudents();
        assertEquals(1, students.size());
        assertEquals(studentCId, students.iterator().next().getId());
    }

    private ResultActions deleteGroupMembersByIds(int groupId, int... studentIds) throws Exception {
        String ids = Arrays.stream(studentIds)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        return mockMvc.perform(delete(GROUP_PATH + "/{groupId}/students", groupId)
                .queryParam("ids", ids));
    }

}
