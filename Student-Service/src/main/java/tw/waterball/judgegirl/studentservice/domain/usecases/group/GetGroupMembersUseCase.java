package tw.waterball.judgegirl.studentservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetGroupMembersUseCase {

    private final GroupRepository groupRepository;

    public void execute(int groupId, Presenter presenter)
            throws NotFoundException {
        Group group = findGroup(groupId);
        presenter.setStudents(new ArrayList<>(group.getStudents()));
    }

    private Group findGroup(int groupId) {
        return groupRepository.findGroupById(groupId)
                .orElseThrow(() -> notFound(Group.class).id(groupId));
    }

    public interface Presenter {

        void setStudents(List<Student> students);

    }
}
