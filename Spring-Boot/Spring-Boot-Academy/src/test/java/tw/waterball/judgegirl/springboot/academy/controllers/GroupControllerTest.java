package tw.waterball.judgegirl.springboot.academy.controllers;

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
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.academy.domain.usecases.group.CreateGroupUseCase;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Group;
import tw.waterball.judgegirl.springboot.academy.SpringBootAcademyApplication;
import tw.waterball.judgegirl.springboot.academy.view.GroupView;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.studentapi.clients.FakeStudentServiceDriver;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.studentapi.clients.view.StudentView.toViewModel;

/**
 * @author - wally55077@gmail.com
 */
@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = {SpringBootAcademyApplication.class})
public class GroupControllerTest extends AbstractSpringBootTest {
    private static final String GROUP_NAME = "groupName";
    private static final String GROUP_PATH = "/api/groups";

    @Autowired
    private FakeStudentServiceDriver studentServiceDriver;

    @Autowired
    private GroupRepository groupRepository;

    @AfterEach
    void cleanUp() {
        groupRepository.deleteAll();
        studentServiceDriver.clear();
    }

    @Test
    public void WhenCreateGroupWithUniqueName_ShouldCreateSuccessfully() throws Exception {
        GroupView group = createGroupAndGet();

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

        var groups = getAllGroups();

        assertEquals(2, groups.size());
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

    @Test
    public void WhenDeleteGroupByNonExistingGroupId_ShouldRespondOk() throws Exception {
        int nonExistingGroupId = 123123;
        deleteGroupById(nonExistingGroupId)
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void GivenOneGroupCreated_WhenAddTwoGroupMembers_ShouldAddSuccessfully() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        var studentViewA = signUpStudent("A");
        var studentViewB = signUpStudent("B");

        addGroupMember(groupId, studentViewA.id);
        addGroupMember(groupId, studentViewB.id);

        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        assertEquals(2, group.getMemberIds().size());
        assertEquals(1, getOwnGroups(studentViewA.id).size());
        assertEquals(1, getOwnGroups(studentViewB.id).size());
    }

    @Test
    public void GivenOneGroupMemberAddedIntoCreatedGroup_WhenAddGroupMemberMultipleTimesIntoTheGroup_ShouldRespondOk() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        var studentA = signUpStudent("A");
        addGroupMember(groupId, studentA.id);

        addGroupMember(groupId, studentA.id)
                .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void GivenTwoGroupMembersAddedIntoCreatedGroup_WhenDeleteOneGroupMemberFromTheGroup_ThenGroupShouldHaveOneGroupMember() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        var studentA = signUpStudent("A");
        var studentB = signUpStudent("B");
        addGroupMember(groupId, studentA.id);
        addGroupMember(groupId, studentB.id);

        deleteGroupMember(groupId, studentA.id);

        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        var students = getGroupMembers(group.getId());
        assertEquals(1, students.size());
        assertEquals(studentB.id, students.get(0).id);
    }

    @Test
    @Transactional
    public void GivenTwoGroupMembersAddedIntoCreatedGroup_WhenDeleteGroupById_ShouldDeleteSuccessfully() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        var studentA = signUpStudent("A");
        var studentB = signUpStudent("B");
        addGroupMember(groupId, studentA.id);
        addGroupMember(groupId, studentB.id);

        deleteGroupById(groupId);

        anotherTransaction(() -> assertEquals(0, getOwnGroups(studentA.id).size()));
    }

    @Test
    public void GivenTwoGroupMembersAddedIntoCreatedGroup_WhenGetGroupMembersByGroupId_ShouldRespondTwoGroupMembers() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        var studentA = signUpStudent("A");
        var studentB = signUpStudent("B");
        addGroupMember(groupId, studentA.id);
        addGroupMember(groupId, studentB.id);

        var members = getGroupMembers(groupId);
        assertEquals(2, members.size());
    }

    @Test
    public void GivenOneGroupMemberAddedIntoTwoCreatedGroups_WhenGetGroupsByStudentId_ShouldRespondTwoGroups() throws Exception {
        int groupAId = createGroupAndGet(GROUP_NAME + "A").id;
        int groupBId = createGroupAndGet(GROUP_NAME + "B").id;
        var studentA = signUpStudent("A");
        int studentId = studentA.id;
        addGroupMember(groupAId, studentId);
        addGroupMember(groupBId, studentId);

        var groups = getOwnGroups(studentId);
        assertEquals(2, groups.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void GivenOneGroupCreated_WhenAddTwoGroupMembersIntoTheGroupByMailList_ShouldRespondEmptyErrorListAndGroupShouldHaveTwoGroupMembers() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        var studentA = signUpStudent("A");
        var studentB = signUpStudent("B");

        String[] mailList = {studentA.email, studentB.email};
        List<String> errorList = (List<String>) getBody(addGroupMembersIntoGroupByMailList(groupId, mailList)
                .andExpect(status().isOk()), Map.class).get("errorList");

        assertTrue(errorList.isEmpty());
        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        assertEquals(2, group.getMemberIds().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void GivenTwoGroupMembersAddedIntoCreatedGroup_WhenAddSameGroupMembersMultipleTimesIntoTheGroupByMailList_ShouldRespondEmptyErrorListAndGroupShouldHaveTwoGroupMembers() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        var studentA = signUpStudent("A");
        var studentB = signUpStudent("B");
        addGroupMember(groupId, studentA.id);
        addGroupMember(groupId, studentB.id);

        String[] mailList = {studentA.email, studentB.email};
        List<String> errorList = (List<String>) getBody(addGroupMembersIntoGroupByMailList(groupId, mailList)
                .andExpect(status().isOk()), Map.class).get("errorList");

        assertTrue(errorList.isEmpty());
        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        assertEquals(2, group.getMemberIds().size());
    }

    @SuppressWarnings("unchecked")
    @DisplayName("Given group member A(A@gmail.com) and a Group, " +
            "When add group members by mail-list (A@gmail.com, B@gmail.com), " +
            "Should return an errorList with [B@gmail.com]")
    @Test
    public void testAddGroupMembersIntoGroupByNonExistingEmails() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        var studentA = signUpStudent("A");

        String nonExistingStudentEmail = "B@gmail.com";
        String[] mailList = {studentA.email, nonExistingStudentEmail};
        List<String> errorList = (List<String>) getBody(addGroupMembersIntoGroupByMailList(groupId, mailList)
                .andExpect(status().isOk()), Map.class).get("errorList");

        assertEquals(nonExistingStudentEmail, errorList.get(0));
        Group group = groupRepository.findGroupById(groupId).orElseThrow();
        assertEquals(1, group.getMemberIds().size());
    }

    @Test
    @Transactional
    public void GivenGroupMembers_A_B_C_AddedIntoGroup_WhenDeleteStudentsByIds_A_B_ShouldRemainGroupMemberCInGroup() throws Exception {
        int groupId = createGroupAndGet(GROUP_NAME).id;
        int studentAId = signUpStudent("A").id;
        int studentBId = signUpStudent("B").id;
        int studentCId = signUpStudent("C").id;
        addGroupMember(groupId, studentAId);
        addGroupMember(groupId, studentBId);
        addGroupMember(groupId, studentCId);

        anotherTransaction(() -> deleteGroupMembersByIds(groupId, studentAId, studentBId)
                .andExpect(status().isOk()));

        anotherTransaction(() -> {
            Group group = groupRepository.findGroupById(groupId).orElseThrow();
            var students = getGroupMembers(group.getId());
            assertEquals(1, students.size());
            assertEquals(studentCId, students.get(0).id);
        });
    }

    private List<GroupView> getAllGroups() throws Exception {
        return getBody(mockMvc.perform(
                withAdminToken(get(GROUP_PATH)))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private GroupView createGroupAndGet() throws Exception {
        return createGroupAndGet(GROUP_NAME);
    }

    private GroupView createGroupAndGet(String groupName) throws Exception {
        return getBody(createGroup(groupName), GroupView.class);
    }

    private ResultActions createGroup(String groupName) throws Exception {
        CreateGroupUseCase.Request request = new CreateGroupUseCase.Request(groupName);
        return mockMvc.perform(withAdminToken(post(GROUP_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
    }

    private ResultActions getGroupById(Integer id) throws Exception {
        return mockMvc.perform(withAdminToken(get(GROUP_PATH + "/{groupId}", id)));
    }

    private ResultActions addGroupMembersIntoGroupByMailList(int groupId, String[] mailList) throws Exception {
        return mockMvc.perform(withAdminToken(post(GROUP_PATH + "/{groupId}/members", groupId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(mailList)));
    }

    private List<GroupView> getOwnGroups(int studentId) throws Exception {
        return getBody(mockMvc.perform(withStudentToken(studentId,
                get("/api/members/{studentId}/groups", studentId)))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private StudentView signUpStudent(String name) {
        String email = name + "@gmail.com";
        String password = "password" + name;
        return signUpStudent(name, email, password);
    }

    private StudentView signUpStudent(String name, String email, String password) {
        Student newStudent = new Student(name, email, password);
        studentServiceDriver.addStudent(newStudent);
        return toViewModel(newStudent);
    }

    private ResultActions addGroupMember(int groupId, int studentId) throws Exception {
        return mockMvc.perform(withAdminToken(
                post(GROUP_PATH + "/{groupId}/members/{studentId}", groupId, studentId)));
    }

    private ResultActions deleteGroupById(Integer id) throws Exception {
        return mockMvc.perform(withAdminToken(
                delete(GROUP_PATH + "/{groupId}", id)));
    }


    private ResultActions deleteGroupMember(int groupId, int studentId) throws Exception {
        return mockMvc.perform(
                withAdminToken(delete(GROUP_PATH + "/{groupId}/members/{studentId}", groupId, studentId)));
    }

    private List<StudentView> getGroupMembers(int groupId) throws Exception {
        return getBody(mockMvc.perform(withAdminToken(
                get(GROUP_PATH + "/{groupId}/members", groupId)))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private ResultActions deleteGroupMembersByIds(int groupId, int... studentIds) throws Exception {
        String ids = Arrays.stream(studentIds)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
        return mockMvc.perform(withAdminToken(
                delete(GROUP_PATH + "/{groupId}/members", groupId))
                .queryParam("ids", ids));
    }

}
