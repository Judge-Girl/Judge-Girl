package tw.waterball.judgegirl.academy.domain.usecases.group;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.exceptions.DuplicateGroupNameException;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.primitives.exam.Group;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class CreateGroupUseCase extends AbstractGroupUseCase {

    public CreateGroupUseCase(GroupRepository groupRepository) {
        super(groupRepository);
    }

    public void execute(Request request, Presenter presenter) throws DuplicateGroupNameException {
        Group group = new Group(request.name);
        groupNameShouldBeDistinct(group);
        presenter.showGroup(groupRepository.save(group));
    }

    private void groupNameShouldBeDistinct(Group group) throws DuplicateGroupNameException {
        if (groupRepository.existsByName(group.getName())) {
            throw new DuplicateGroupNameException();
        }
    }

    public interface Presenter {
        void showGroup(Group group);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public String name;
    }
}
