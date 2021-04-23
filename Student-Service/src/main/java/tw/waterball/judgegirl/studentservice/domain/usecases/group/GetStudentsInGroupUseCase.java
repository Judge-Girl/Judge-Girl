package tw.waterball.judgegirl.studentservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetStudentsInGroupUseCase {

    private final GroupRepository groupRepository;

    public void execute(int groupId, Presenter presenter)
            throws NotFoundException {
        Group group = groupRepository.findGroupById(groupId)
                .orElseThrow(NotFoundException::new);
        presenter.setStudents(new ArrayList<>(group.getStudents()));
    }

    public interface Presenter {

        void setStudents(List<Student> students);

    }
}
