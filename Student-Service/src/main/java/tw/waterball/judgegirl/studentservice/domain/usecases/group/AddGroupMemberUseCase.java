package tw.waterball.judgegirl.studentservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class AddGroupMemberUseCase {

    private final StudentRepository studentRepository;

    private final GroupRepository groupRepository;

    public void execute(Request request)
            throws NotFoundException {
        // TODO: improve performance, should only perform one SQL query
        int groupId = request.groupId;
        int studentId = request.studentId;
        Group group = groupRepository.findGroupById(groupId)
                .orElseThrow(NotFoundException::new);
        if (!hasStudentAddedIntoGroup(group, studentId)) {
            Student student = studentRepository.findStudentById(studentId)
                    .orElseThrow(NotFoundException::new);
            group.addStudent(student);
            groupRepository.save(group);
        }
    }

    private boolean hasStudentAddedIntoGroup(Group group, int studentId) {
        return group.getStudents()
                .stream()
                .map(Student::getId)
                .anyMatch(groupStudentId -> studentId == groupStudentId);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int groupId;
        public int studentId;
    }

}
