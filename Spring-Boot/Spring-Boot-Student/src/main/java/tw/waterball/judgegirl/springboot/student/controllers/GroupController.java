package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.view.GroupView;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateGroupNameException;
import tw.waterball.judgegirl.studentservice.domain.usecases.*;
import tw.waterball.judgegirl.studentservice.domain.usecases.AddStudentsIntoGroupByMailListUseCase.Response;

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
    private final GetStudentsByGroupIdUseCase getStudentsByGroupIdUseCase;
    private final GetGroupsByStudentIdUseCase getGroupsByStudentIdUseCase;
    private final AddStudentsIntoGroupByMailListUseCase addStudentsIntoGroupByMailListUseCase;

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

    @GetMapping("/api/groups/{groupId}/students")
    public List<StudentView> getStudentsByGroupId(@PathVariable Integer groupId) {
        GetStudentsByGroupIdPresenter presenter = new GetStudentsByGroupIdPresenter();
        getStudentsByGroupIdUseCase.execute(groupId, presenter);
        return presenter.present();
    }

    @GetMapping("/api/students/{studentId}/groups")
    public List<GroupView> getGroupsByStudentId(@PathVariable Integer studentId) {
        GetGroupsByStudentIdPresenter presenter = new GetGroupsByStudentIdPresenter();
        getGroupsByStudentIdUseCase.execute(studentId, presenter);
        return presenter.present();
    }

    @PostMapping("/api/groups/{groupId}/students")
    public Response getStudentsByGroupId(@PathVariable Integer groupId,
                                         @RequestBody String[] mailList) {
        AddStudentsIntoGroupByMailListPresenter presenter = new AddStudentsIntoGroupByMailListPresenter();
        addStudentsIntoGroupByMailListUseCase.execute(groupId, mailList, presenter);
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

class GetStudentsByGroupIdPresenter implements GetStudentsByGroupIdUseCase.Presenter {

    private List<Student> students;

    @Override
    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<StudentView> present() {
        return students.stream().map(StudentView::toViewModel).collect(Collectors.toList());
    }
}

class GetGroupsByStudentIdPresenter implements GetGroupsByStudentIdUseCase.Presenter {

    private List<Group> groups;

    @Override
    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<GroupView> present() {
        return groups.stream().map(GroupView::toViewModel).collect(Collectors.toList());
    }
}

class AddStudentsIntoGroupByMailListPresenter implements AddStudentsIntoGroupByMailListUseCase.Presenter {

    private String[] errorList;

    @Override
    public void setErrorList(String... errorList) {
        this.errorList = errorList;
    }

    public Response present() {
        return new Response(errorList);
    }

}
