package tw.waterball.judgegirl.studentservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class AddGroupMembersByMailListUseCase {

    private final StudentRepository studentRepository;

    private final GroupRepository groupRepository;

    public void execute(Request request, Presenter presenter)
            throws NotFoundException {
        int groupId = request.groupId;
        String[] mailList = request.mailList;
        Group group = findGroup(groupId);
        List<Student> students = studentRepository.findByEmailIn(mailList);
        List<Student> newStudents = filterStudentsThatHaveNotBeenAdded(group, students);
        if (!newStudents.isEmpty()) {
            group.addStudents(newStudents);
            groupRepository.save(group);
        }
        presenter.notFound(getNotFoundMailList(mailList, students));
    }

    private Group findGroup(int groupId) {
        return groupRepository.findGroupById(groupId)
                .orElseThrow(() -> notFound(Group.class).id(groupId));
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

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int groupId;
        public String[] mailList;
    }

    public interface Presenter {

        void notFound(String... errorList);

    }

}
