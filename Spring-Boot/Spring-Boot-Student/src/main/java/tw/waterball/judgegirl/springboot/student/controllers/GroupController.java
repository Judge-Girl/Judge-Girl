package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentapi.clients.view.GroupView;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateGroupNameException;
import tw.waterball.judgegirl.studentservice.domain.usecases.group.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author - wally55077@gmail.com
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final GetGroupUseCase getGroupUseCase;
    private final GetAllGroupsUseCase getAllGroupsUseCase;
    private final DeleteGroupUseCase deleteGroupUseCase;
    private final AddGroupMemberUseCase addGroupMemberUseCase;
    private final DeleteGroupMembersUseCase deleteGroupMembersUseCase;
    private final GetGroupMembersUseCase getGroupMembersUseCase;
    private final GetGroupsOwnedByGroupMemberUseCase getGroupsOwnedByGroupMemberUseCase;
    private final AddGroupMembersByMailListUseCase addGroupMembersByMailListUseCase;

    @PostMapping("/groups")
    public GroupView createGroup(@RequestBody CreateGroupUseCase.Request request) {
        CreateGroupPresenter presenter = new CreateGroupPresenter();
        createGroupUseCase.execute(request, presenter);
        return presenter.present();
    }

    @GetMapping("/groups/{groupId}")
    public GroupView getGroupById(@PathVariable Integer groupId) {
        GetGroupPresenter presenter = new GetGroupPresenter();
        getGroupUseCase.execute(groupId, presenter);
        return presenter.present();
    }

    @GetMapping("/groups")
    public List<GroupView> getAllGroups() {
        GetAllGroupsPresenter presenter = new GetAllGroupsPresenter();
        getAllGroupsUseCase.execute(presenter);
        return presenter.present();
    }

    @DeleteMapping("/groups/{groupId}")
    public void deleteGroupById(@PathVariable Integer groupId) {
        deleteGroupUseCase.execute(groupId);
    }

    @PostMapping("/groups/{groupId}/students/{studentId}")
    public void addGroupMember(@PathVariable Integer groupId,
                               @PathVariable Integer studentId) {
        addGroupMemberUseCase.execute(new AddGroupMemberUseCase.Request(groupId, studentId));
    }

    @DeleteMapping("/groups/{groupId}/students/{studentId}")
    public void deleteGroupMember(@PathVariable Integer groupId,
                                  @PathVariable Integer studentId) {
        deleteGroupMembersUseCase.execute(new DeleteGroupMembersUseCase.Request(groupId, Collections.singletonList(studentId)));
    }

    @GetMapping("/groups/{groupId}/students")
    public List<StudentView> getGroupMembers(@PathVariable Integer groupId) {
        GetGroupMembersPresenter presenter = new GetGroupMembersPresenter();
        getGroupMembersUseCase.execute(groupId, presenter);
        return presenter.present();
    }

    @GetMapping("/students/{studentId}/groups")
    public List<GroupView> getGroupsByGroupMemberId(@PathVariable Integer studentId) {
        GetGroupsOwnedByGroupMemberPresenter presenter = new GetGroupsOwnedByGroupMemberPresenter();
        getGroupsOwnedByGroupMemberUseCase.execute(studentId, presenter);
        return presenter.present();
    }

    @PostMapping("/groups/{groupId}/students")
    public Map<String, List<String>> getGroupMembers(@PathVariable Integer groupId,
                                                     @RequestBody String[] mailList) {
        AddGroupMembersByMailListUseCase.Request request = new AddGroupMembersByMailListUseCase.Request(groupId, mailList);
        AddGroupMembersIntoGroupByMailListPresenter presenter = new AddGroupMembersIntoGroupByMailListPresenter();
        addGroupMembersByMailListUseCase.execute(request, presenter);
        return presenter.present();
    }

    @DeleteMapping("/groups/{groupId}/students")
    public void deleteGroupMembersByIds(@PathVariable Integer groupId,
                                        @RequestParam Integer[] ids) {
        deleteGroupMembersUseCase.execute(new DeleteGroupMembersUseCase.Request(groupId, Arrays.asList(ids)));
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

class GetGroupPresenter implements GetGroupUseCase.Presenter {

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

class GetGroupMembersPresenter implements GetGroupMembersUseCase.Presenter {

    private List<Student> students;

    @Override
    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<StudentView> present() {
        return students.stream().map(StudentView::toViewModel).collect(Collectors.toList());
    }
}

class GetGroupsOwnedByGroupMemberPresenter implements GetGroupsOwnedByGroupMemberUseCase.Presenter {

    private List<Group> groups;

    @Override
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<GroupView> present() {
        return groups.stream().map(GroupView::toViewModel).collect(Collectors.toList());
    }
}

class AddGroupMembersIntoGroupByMailListPresenter implements AddGroupMembersByMailListUseCase.Presenter {

    private String[] errorList;

    @Override
    public void notFound(String... errorList) {
        this.errorList = errorList;
    }

    public Map<String, List<String>> present() {
        return Collections.singletonMap("errorList", Arrays.asList(errorList));
    }

}
