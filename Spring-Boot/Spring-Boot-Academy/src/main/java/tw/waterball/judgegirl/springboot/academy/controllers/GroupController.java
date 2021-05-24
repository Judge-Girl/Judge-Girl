package tw.waterball.judgegirl.springboot.academy.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.academy.domain.usecases.group.*;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Group;
import tw.waterball.judgegirl.springboot.academy.view.GroupView;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;

import java.util.Collections;
import java.util.List;
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
    private final TokenService tokenService;
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
    public GroupView createGroup(@RequestHeader("Authorization") String authorization,
                                 @RequestBody CreateGroupUseCase.Request request) {
        return tokenService.returnIfAdmin(authorization, token -> {
            CreateGroupPresenter presenter = new CreateGroupPresenter();
            createGroupUseCase.execute(request, presenter);
            return presenter.present();
        });
    }

    @GetMapping("/groups/{groupId}")
    public GroupView getGroupById(@RequestHeader("Authorization") String authorization,
                                  @PathVariable Integer groupId) {
        return tokenService.returnIfAdmin(authorization, token -> {
            GetGroupPresenter presenter = new GetGroupPresenter();
            getGroupUseCase.execute(groupId, presenter);
            return presenter.present();
        });
    }

    @GetMapping("/groups")
    public List<GroupView> getAllGroups(@RequestHeader("Authorization") String authorization) {
        return tokenService.returnIfAdmin(authorization, token -> {
            GetAllGroupsPresenter presenter = new GetAllGroupsPresenter();
            getAllGroupsUseCase.execute(presenter);
            return presenter.present();
        });
    }

    @DeleteMapping("/groups/{groupId}")
    public void deleteGroupById(@RequestHeader("Authorization") String authorization,
                                @PathVariable Integer groupId) {
        tokenService.ifAdminToken(authorization, token -> deleteGroupUseCase.execute(groupId));
    }

    @PostMapping("/groups/{groupId}/members/{memberId}")
    public void addGroupMember(@RequestHeader("Authorization") String authorization,
                               @PathVariable Integer groupId,
                               @PathVariable Integer memberId) {
        tokenService.ifAdminToken(authorization,
                token -> addGroupMemberUseCase.execute(new AddGroupMemberUseCase.Request(groupId, memberId)));
    }

    @DeleteMapping("/groups/{groupId}/members/{memberId}")
    public void deleteGroupMember(@RequestHeader("Authorization") String authorization,
                                  @PathVariable Integer groupId,
                                  @PathVariable Integer memberId) {
        tokenService.ifAdminToken(authorization,
                token -> deleteGroupMembersUseCase.execute(new DeleteGroupMembersUseCase.Request(groupId, Collections.singletonList(memberId))));
    }

    @GetMapping("/groups/{groupId}/members")
    public List<StudentView> getGroupMembers(@RequestHeader("Authorization") String authorization,
                                             @PathVariable Integer groupId) {
        return tokenService.returnIfAdmin(authorization, token -> {
            GetGroupMembersPresenter presenter = new GetGroupMembersPresenter();
            getGroupMembersUseCase.execute(groupId, presenter);
            return presenter.present();
        });
    }

    @PostMapping("/groups/{groupId}/members")
    public AddGroupMembersIntoGroupByMailListPresenter.View addGroupMembers(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Integer groupId,
            @RequestBody String[] mailList) {
        return tokenService.returnIfAdmin(authorization, token -> {
            AddGroupMembersByMailListUseCase.Request request = new AddGroupMembersByMailListUseCase.Request(groupId, asList(mailList));
            AddGroupMembersIntoGroupByMailListPresenter presenter = new AddGroupMembersIntoGroupByMailListPresenter();
            addGroupMembersByMailListUseCase.execute(request, presenter);
            return presenter.present();
        });
    }

    @DeleteMapping("/groups/{groupId}/members")
    public void deleteGroupMembersByIds(@RequestHeader("Authorization") String authorization,
                                        @PathVariable Integer groupId,
                                        @RequestParam Integer[] ids) {
        tokenService.ifAdminToken(authorization, token ->
                deleteGroupMembersUseCase.execute(new DeleteGroupMembersUseCase.Request(groupId, asList(ids))));
    }

    @GetMapping("/members/{memberId}/groups")
    public List<GroupView> getOwnGroups(@RequestHeader("Authorization") String authorization,
                                        @PathVariable Integer memberId) {
        return tokenService.returnIfGranted(memberId, authorization, token -> {
            GetOwnGroupsPresenter presenter = new GetOwnGroupsPresenter();
            getOwnGroupsUseCase.execute(memberId, presenter);
            return presenter.present();
        });
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

    private List<Student> members;

    @Override
    public void setMembers(List<Student> members) {
        this.members = members;
    }

    public List<StudentView> present() {
        return members.stream().map(StudentView::toViewModel).collect(toList());
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

    public View present() {
        return new View(errorEmailList);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class View {
        public List<String> errorList;
    }
}
