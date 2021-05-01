package tw.waterball.judgegirl.springboot.exam.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.entities.exam.Group;
import tw.waterball.judgegirl.examservice.domain.usecases.group.*;
import tw.waterball.judgegirl.studentapi.clients.view.GroupView;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

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
    private final GetOwnGroupsUseCase getOwnGroupsUseCase;
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

    @PostMapping("/groups/{groupId}/members/{studentId}")
    public void addGroupMember(@PathVariable Integer groupId,
                               @PathVariable Integer studentId) {
        addGroupMemberUseCase.execute(new AddGroupMemberUseCase.Request(groupId, studentId));
    }

    @DeleteMapping("/groups/{groupId}/members/{memberId}")
    public void deleteGroupMember(@PathVariable Integer groupId,
                                  @PathVariable Integer memberId) {
        deleteGroupMembersUseCase.execute(new DeleteGroupMembersUseCase.Request(groupId, Collections.singletonList(memberId)));
    }

    @GetMapping("/groups/{groupId}/members")
    public List<StudentView> getGroupMembers(@PathVariable Integer groupId) {
        GetGroupMembersPresenter presenter = new GetGroupMembersPresenter();
        getGroupMembersUseCase.execute(groupId, presenter);
        return presenter.present();
    }

    @GetMapping("/members/{memberId}/groups")
    public List<GroupView> getOwnGroups(@PathVariable Integer memberId) {
        GetOwnGroupsPresenter presenter = new GetOwnGroupsPresenter();
        getOwnGroupsUseCase.execute(memberId, presenter);
        return presenter.present();
    }

    @PostMapping("/groups/{groupId}/members")
    public Map<String, List<String>> getGroupMembers(@PathVariable Integer groupId,
                                                     @RequestBody String[] mailList) {
        AddGroupMembersByMailListUseCase.Request request = new AddGroupMembersByMailListUseCase.Request(groupId, asList(mailList));
        AddGroupMembersIntoGroupByMailListPresenter presenter = new AddGroupMembersIntoGroupByMailListPresenter();
        addGroupMembersByMailListUseCase.execute(request, presenter);
        return presenter.present();
    }

    @DeleteMapping("/groups/{groupId}/members")
    public void deleteGroupMembersByIds(@PathVariable Integer groupId,
                                        @RequestParam Integer[] ids) {
        deleteGroupMembersUseCase.execute(new DeleteGroupMembersUseCase.Request(groupId, asList(ids)));
    }

}

class CreateGroupPresenter implements CreateGroupUseCase.Presenter {

    private Group group;

    @Override
    public void showGroup(Group group) {
        this.group = group;
    }

    public GroupView present() {
        return GroupView.toViewModel(group);
    }

}

class GetGroupPresenter implements GetGroupUseCase.Presenter {

    private Group group;

    @Override
    public void showGroup(Group group) {
        this.group = group;
    }

    public GroupView present() {
        return GroupView.toViewModel(group);
    }

}


class GetAllGroupsPresenter implements GetAllGroupsUseCase.Presenter {

    private List<Group> groups;

    @Override
    public void showGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<GroupView> present() {
        return groups.stream().map(GroupView::toViewModel).collect(toList());
    }

}

class GetGroupMembersPresenter implements GetGroupMembersUseCase.Presenter {

    private List<Student> students;

    @Override
    public void setMembers(List<Student> members) {
        this.students = members;
    }

    public List<StudentView> present() {
        return students.stream().map(StudentView::toViewModel).collect(toList());
    }
}

class GetOwnGroupsPresenter implements GetOwnGroupsUseCase.Presenter {

    private Set<Group> groups;

    @Override
    public void showGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public List<GroupView> present() {
        return groups.stream().map(GroupView::toViewModel).collect(toList());
    }
}

class AddGroupMembersIntoGroupByMailListPresenter implements AddGroupMembersByMailListUseCase.Presenter {

    private List<String> errorEmailList;

    @Override
    public void notFound(List<String> errorEmailList) {
        this.errorEmailList = errorEmailList;
    }

    public Map<String, List<String>> present() {
        return Collections.singletonMap("errorList", errorEmailList);
    }
}
