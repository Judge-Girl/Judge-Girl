package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.springboot.student.view.GroupView;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateGroupNameException;
import tw.waterball.judgegirl.studentservice.domain.usecases.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - wally55077@gmail.com
 */
@CrossOrigin
@RestController
@AllArgsConstructor
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final GetGroupByIdUseCase getGroupByIdUseCase;
    private final GetAllGroupsUseCase getAllGroupsUseCase;
    private final DeleteGroupByIdUseCase deleteGroupByIdUseCase;
    private final AddStudentIntoGroupUseCase addStudentIntoGroupUseCase;
    private final DeleteStudentFromGroupUseCase deleteStudentFromGroupUseCase;

    @PostMapping("/api/groups")
    public GroupView createGroup(@RequestBody CreateGroupUseCase.Request request) {
        CreateGroupPresenter presenter = new CreateGroupPresenter();
        createGroupUseCase.execute(request, presenter);
        return presenter.present();
    }

    @GetMapping("/api/groups/{groupId}")
    public GroupView getGroupById(@PathVariable Integer groupId) {
        GetGroupByIdPresenter presenter = new GetGroupByIdPresenter();
        getGroupByIdUseCase.execute(groupId, presenter);
        return presenter.present();
    }

    @GetMapping("/api/groups")
    public List<GroupView> getAllGroups() {
        GetAllGroupsPresenter presenter = new GetAllGroupsPresenter();
        getAllGroupsUseCase.execute(presenter);
        return presenter.present();
    }

    @DeleteMapping("/api/groups/{groupId}")
    public void deleteGroupById(@PathVariable Integer groupId) {
        deleteGroupByIdUseCase.execute(groupId);
    }

    @PostMapping("/api/groups/{groupId}/students/{studentId}")
    public void addStudentIntoGroup(@PathVariable Integer groupId,
                                    @PathVariable Integer studentId) {
        addStudentIntoGroupUseCase.execute(groupId, studentId);
    }

    @DeleteMapping("/api/groups/{groupId}/students/{studentId}")
    public void deleteStudentFromGroup(@PathVariable Integer groupId,
                                       @PathVariable Integer studentId) {
        deleteStudentFromGroupUseCase.execute(groupId, studentId);
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

    public GroupView present() {
        return GroupView.toViewModel(group);
    }

}

class GetGroupByIdPresenter implements GetGroupByIdUseCase.Presenter {

    private Group group;

    @Override
    public void setGroup(Group group) {
        this.group = group;
    }

    public GroupView present() {
        return GroupView.toViewModel(group);
    }

}


class GetAllGroupsPresenter implements GetAllGroupsUseCase.Presenter {

    private List<Group> groups;

    @Override
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<GroupView> present() {
        return groups.stream().map(GroupView::toViewModel).collect(Collectors.toList());
    }

}

