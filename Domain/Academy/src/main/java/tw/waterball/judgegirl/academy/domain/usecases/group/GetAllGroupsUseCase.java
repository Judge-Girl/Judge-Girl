package tw.waterball.judgegirl.academy.domain.usecases.group;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.primitives.exam.Group;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;

import javax.inject.Named;
import java.util.List;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class GetAllGroupsUseCase {

    private final GroupRepository groupRepository;

    public void execute(Presenter presenter) {
        List<Group> groups = groupRepository.findAllGroups();
        presenter.showGroups(groups);
    }

    public interface Presenter {

        void showGroups(List<Group> groups);

    }

}
