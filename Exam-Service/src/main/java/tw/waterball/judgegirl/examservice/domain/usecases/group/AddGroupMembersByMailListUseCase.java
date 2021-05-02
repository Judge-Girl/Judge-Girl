package tw.waterball.judgegirl.examservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.entities.exam.Group;
import tw.waterball.judgegirl.entities.exam.MemberId;
import tw.waterball.judgegirl.examservice.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;

import javax.inject.Named;
import java.util.List;
import java.util.Set;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.filterToList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToSet;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class AddGroupMembersByMailListUseCase extends AbstractGroupUseCase {
    private final StudentServiceDriver studentServiceDriver;

    public AddGroupMembersByMailListUseCase(GroupRepository groupRepository, StudentServiceDriver studentServiceDriver) {
        super(groupRepository);
        this.studentServiceDriver = studentServiceDriver;
    }

    public void execute(Request request, Presenter presenter) throws NotFoundException {
        int groupId = request.groupId;
        List<String> mailList = request.mailList;
        Group group = findGroup(groupId);

        List<Student> students = studentServiceDriver.getStudentsByEmails(mailList);
        List<Student> newMembers = filterNewMembers(group, students);
        if (!newMembers.isEmpty()) {
            group.addStudentsAsMembers(newMembers);
            groupRepository.save(group);
        }
        presenter.notFound(getNotFoundMailList(mailList, students));
    }

    private List<Student> filterNewMembers(Group group, List<Student> students) {
        Set<Integer> idSet = mapToSet(group.getMemberIds(), MemberId::getId);
        return filterToList(students,
                student -> !idSet.contains(student.getId()));
    }

    private List<String> getNotFoundMailList(List<String> mailList, List<Student> students) {
        if (students.isEmpty()) {
            return mailList;
        }
        Set<String> mails = mapToSet(students, Student::getEmail);
        return filterToList(mailList, email -> !mails.contains(email));
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int groupId;
        public List<String> mailList;
    }

    public interface Presenter {
        void notFound(List<String> errorEmailList);
    }

}
