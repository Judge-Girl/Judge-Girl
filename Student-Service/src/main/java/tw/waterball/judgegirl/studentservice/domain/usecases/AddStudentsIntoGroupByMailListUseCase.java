package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;
import java.util.Arrays;
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
        List<Student> newStudents = filterStudentsThatHaveNotBeenAdded(group, students);
        if (!newStudents.isEmpty()) {
            group.addStudents(newStudents);
            groupRepository.save(group);
        }
        presenter.notFound(getNotFoundMailList(mailList, students));
    }

    private List<Student> filterStudentsThatHaveNotBeenAdded(Group group, List<Student> students) {
        Set<Integer> idSet = group.getStudents().stream()
                .map(Student::getId)
                .collect(Collectors.toSet());
        return students.stream()
                .filter(student -> !idSet.contains(student.getId()))
                .collect(Collectors.toList());
    }

    private String[] getNotFoundMailList(String[] mailList, List<Student> students) {
        if (students.isEmpty()) {
            return mailList;
        }
        Set<String> mails = students.stream()
                .map(Student::getEmail)
                .collect(Collectors.toSet());
        return Arrays.stream(mailList)
                .filter(email -> !mails.contains(email))
                .toArray(String[]::new);
    }

    public interface Presenter {

        void notFound(String... errorList);

    }

}
