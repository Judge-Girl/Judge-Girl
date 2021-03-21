package tw.waterball.judgegirl.springboot.student.controllers.it;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.SpringBootStudentApplication;
import tw.waterball.judgegirl.springboot.student.view.GroupView;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentservice.domain.usecases.CreateGroupUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author - wally55077@gmail.com
 */
@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootStudentApplication.class)
public class GroupControllerIT extends AbstractSpringBootTest {

    private static final String TEST_NAME = "name";
    private static final String BASE_PATH = "/api/groups";

    @Autowired
    private GroupRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void WhenCreateGroupWithUniqueName_ShouldCreateSuccessfully() throws Exception {
        ResultActions resultActions = createGroup(TEST_NAME)
                .andExpect(status().isOk());
        GroupView groupView = getBody(resultActions, GroupView.class);
        assertEquals(TEST_NAME, groupView.name);
        assertTrue(repository.existsByName(TEST_NAME));
    }

    @Test
    public void GiveOneGroupCreated_WhenCreateGroupWithDuplicateName_ShouldRejectWithBadRequest() throws Exception {
        createGroup(TEST_NAME);
        createGroup(TEST_NAME).andExpect(status().isBadRequest());
    }

    @Test
    public void GiveOneGroupCreated_WhenGetGroupById_ShouldRespondGroup() throws Exception {
        GroupView group = getBody(createGroup(TEST_NAME), GroupView.class);
        ResultActions resultActions = getGroupById(group.id).andExpect(status().isOk());
        GroupView body = getBody(resultActions, GroupView.class);
        assertEquals(group, body);
    }

    @Test
    public void GiveTwoGroupsCreated_WhenGetAll_ShouldRespondTwoGroups() throws Exception {
        createGroup(TEST_NAME + 1);
        createGroup(TEST_NAME + 2);
        List<GroupView> body = getBody(getAllGroups().andExpect(status().isOk()), new TypeReference<>() {
        });
        assertEquals(2, body.size());
    }

    private ResultActions getAllGroups() throws Exception {
        return mockMvc.perform(get(BASE_PATH));
    }

    private ResultActions createGroup(String groupName) throws Exception {
        CreateGroupUseCase.Request request = new CreateGroupUseCase.Request(groupName);
        return mockMvc.perform(post(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
    }

    @Test
    public void WhenGetGroupByNonExistingGroupId_ShouldRespondNotFound() throws Exception {
        int nonExistingGroupId = 123123;
        getGroupById(nonExistingGroupId).andExpect(status().isNotFound());
    }

    @Test
    public void GiveOneGroupCreated_WhenDeleteGroupById_ShouldDeleteSuccessfully() throws Exception {
        GroupView group = getBody(createGroup(TEST_NAME), GroupView.class);
        int groupId = group.id;
        deleteGroupById(groupId).andExpect(status().isOk());
        getGroupById(groupId).andExpect(status().isNotFound());
    }

    private ResultActions getGroupById(Integer id) throws Exception {
        return mockMvc.perform(get(BASE_PATH + "/{groupId}", id));
    }

    @Test
    public void WhenDeleteGroupByNonExistingGroupId_ShouldRespondNotFound() throws Exception {
        int nonExistingGroupId = 123123;
        deleteGroupById(nonExistingGroupId)
                .andExpect(status().isNotFound());
    }

    private ResultActions deleteGroupById(Integer id) throws Exception {
        return mockMvc.perform(delete(BASE_PATH + "/{groupId}", id));
    }

}
