package tw.waterball.judgegirl.springboot.student.controllers.it;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.SpringBootStudentApplication;
import tw.waterball.judgegirl.springboot.student.controllers.GroupCreateRequest;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupData;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupDataRepository;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author - wally55077@gmail.com
 */
@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootStudentApplication.class)
public class GroupControllerIT extends AbstractSpringBootTest {

    private static final String TEST_TITLE = "title";

    @Autowired
    private GroupDataRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void GiveOneUserCreateGroup_WhenGroupTitleUnique_ShouldCreateSuccessfully() throws Exception {
        ResultActions resultActions = createGroupRequest()
                .andExpect(status().isOk());
        GroupData groupData = getBody(resultActions, GroupData.class);
        assertEquals(TEST_TITLE, groupData.getTitle());
    }

    @Test
    public void GiveOneUserCreateGroup_WhenGroupTitleExist_ShouldCreateFailed() throws Exception {
        createGroupRequest();
        createGroupRequest()
                .andExpect(status().isBadRequest());
    }

    private ResultActions createGroupRequest() throws Exception {
        GroupCreateRequest request = GroupCreateRequest.builder().title(TEST_TITLE).build();
        return mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
    }

}
