package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.AllArgsConstructor;
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
public class AddStudentIntoGroupUseCase {

    private final StudentRepository studentRepository;

    private final GroupRepository groupRepository;

    public void execute(int groupId, int studentId)
            throws NotFoundException {
        // TODO: improve performance, should only perform one SQL query
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

}
