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
import tw.waterball.judgegirl.springboot.student.view.GroupView;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentservice.domain.usecases.CreateGroupUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author - wally55077@gmail.com
 */
@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootStudentApplication.class)
public class GroupControllerIT extends AbstractSpringBootTest {

    private static final String TEST_NAME = "name";

    @Autowired
    private GroupRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    public void WhenCreateGroupWithUniqueName_ShouldCreateSuccessfully() throws Exception {
        ResultActions resultActions = createGroupRequest()
                .andExpect(status().isOk());
        GroupView groupView = getBody(resultActions, GroupView.class);
        assertEquals(TEST_NAME, groupView.name);
        assertTrue(repository.existsByName(TEST_NAME));
    }

    @Test
    public void GiveOneGroupCreated_WhenCreateGroupWithDuplicateName_ShouldRejectWithBadRequest() throws Exception {
        createGroupRequest();
        createGroupRequest()
                .andExpect(status().isBadRequest());
    }

    private ResultActions createGroupRequest() throws Exception {
        CreateGroupUseCase.Request request = new CreateGroupUseCase.Request(TEST_NAME);
        return mockMvc.perform(post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
    }

}
