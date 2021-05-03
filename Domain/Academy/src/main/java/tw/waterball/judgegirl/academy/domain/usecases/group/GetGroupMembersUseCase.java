package tw.waterball.judgegirl.academy.domain.usecases.group;

import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Group;
import tw.waterball.judgegirl.primitives.exam.MemberId;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;

import javax.inject.Named;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetGroupMembersUseCase extends AbstractGroupUseCase {
    private final StudentServiceDriver studentServiceDriver;

    public GetGroupMembersUseCase(GroupRepository groupRepository, StudentServiceDriver studentServiceDriver) {
        super(groupRepository);
        this.studentServiceDriver = studentServiceDriver;
    }

    public void execute(int groupId, Presenter presenter) throws NotFoundException {
        Group group = findGroup(groupId);
        List<Student> members = studentServiceDriver.getStudentsByIds(
                mapToList(group.getMemberIds(), MemberId::getId));
        presenter.setMembers(members);
    }

    public interface Presenter {
        void setMembers(List<Student> members);
    }
}
