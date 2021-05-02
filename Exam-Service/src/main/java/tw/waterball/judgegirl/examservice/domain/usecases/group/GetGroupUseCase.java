package tw.waterball.judgegirl.examservice.domain.usecases.group;

import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.exam.Group;
import tw.waterball.judgegirl.examservice.domain.repositories.GroupRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetGroupUseCase extends AbstractGroupUseCase {

    public GetGroupUseCase(GroupRepository groupRepository) {
        super(groupRepository);
    }

    public void execute(int groupId, Presenter presenter) throws NotFoundException {
        Group group = findGroup(groupId);
        presenter.showGroup(group);
    }

    public interface Presenter {
        void showGroup(Group group);
    }

}
