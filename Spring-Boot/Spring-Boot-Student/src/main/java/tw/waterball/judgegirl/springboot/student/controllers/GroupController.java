package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.springboot.student.view.GroupView;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateGroupNameException;
import tw.waterball.judgegirl.studentservice.domain.usecases.CreateGroupUseCase;

/**
 * @author - wally55077@gmail.com
 */
@RestController
@AllArgsConstructor
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;

    @PostMapping("/api/groups")
    public GroupView createGroup(@RequestBody CreateGroupUseCase.Request request) {
        CreateGroupPresenter presenter = new CreateGroupPresenter();
        createGroupUseCase.execute(request, presenter);
        return presenter.present();
    }

    @ExceptionHandler({DuplicateGroupNameException.class})
    public ResponseEntity<?> badRequestHandler(Exception err) {
        return ResponseEntity.badRequest().body(err.getMessage());
    }
}

class CreateGroupPresenter implements CreateGroupUseCase.Presenter {

    private Group group;

    @Override
    public void setGroup(Group group) {
        this.group = group;
    }

    GroupView present() {
        return GroupView.toViewModel(group);
    }

}
