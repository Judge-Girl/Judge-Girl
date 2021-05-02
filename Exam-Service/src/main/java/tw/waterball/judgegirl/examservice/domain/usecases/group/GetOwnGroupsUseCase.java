package tw.waterball.judgegirl.examservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.exam.Group;
import tw.waterball.judgegirl.examservice.domain.repositories.GroupRepository;

import javax.inject.Named;
import java.util.Set;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class GetOwnGroupsUseCase {
    private final GroupRepository groupRepository;

    public void execute(int memberId, Presenter presenter)
            throws NotFoundException {
        Set<Group> groups = groupRepository.getOwnGroups(memberId);
        presenter.showGroups(groups);
    }

    public interface Presenter {
        void showGroups(Set<Group> groups);
    }


}
