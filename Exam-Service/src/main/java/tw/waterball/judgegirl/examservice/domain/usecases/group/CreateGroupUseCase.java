package tw.waterball.judgegirl.examservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.exam.Group;
import tw.waterball.judgegirl.examservice.domain.exceptions.DuplicateGroupNameException;
import tw.waterball.judgegirl.examservice.domain.repositories.GroupRepository;

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
        validateGroup(group);
        presenter.showGroup(groupRepository.save(group));
    }

    private void validateGroup(Group group) throws DuplicateGroupNameException {
        group.validate();
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
