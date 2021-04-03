package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class AddStudentsIntoGroupByMailListUseCase {

    private final StudentRepository studentRepository;

    private final GroupRepository groupRepository;

    public void execute(int groupId, String[] mailList, Presenter presenter)
            throws NotFoundException {
        Group group = groupRepository.findGroupById(groupId).orElseThrow(NotFoundException::new);
        List<Student> students = studentRepository.findByEmailIn(mailList);
        List<Student> nonAddedIntoGroupStudents = filterAddedStudents(group, students);
        if (!nonAddedIntoGroupStudents.isEmpty()) {
            group.addStudents(nonAddedIntoGroupStudents);
            groupRepository.save(group);
        }
        presenter.setErrorList(getErrorList(mailList, students));
    }

    private List<Student> filterAddedStudents(Group group, List<Student> students) {
        return students.stream()
                .filter(student -> !hasStudentAddedIntoGroup(group, student))
                .collect(Collectors.toList());
    }

    private boolean hasStudentAddedIntoGroup(Group group, Student student) {
        return group.getStudents()
                .stream()
                .map(Student::getId)
                .anyMatch(groupStudentId -> groupStudentId.equals(student.getId()));
    }

    private String[] getErrorList(String[] mailList, List<Student> students) {
        if (students.isEmpty()) {
            return mailList;
        }
        Set<String> mails = new HashSet<>(Arrays.asList(mailList));
        return students.stream()
                .map(Student::getEmail)
                .filter(email -> !mails.contains(email))
                .toArray(String[]::new);
    }

    public interface Presenter {

        void setErrorList(String... errorList);

    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        public List<String> errorList;

        public Response(String... errorList) {
            this(Arrays.asList(errorList));
        }
    }

}
