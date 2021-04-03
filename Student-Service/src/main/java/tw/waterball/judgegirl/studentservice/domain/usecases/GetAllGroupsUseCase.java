package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import javax.inject.Named;
import java.util.List;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetAllGroupsUseCase {

    private final GroupRepository groupRepository;

    public void execute(Presenter presenter) {
        List<Group> groups = groupRepository.findAllGroups();
        presenter.setGroups(groups);
    }

    public interface Presenter {

        void setGroups(List<Group> groups);

    }

}
