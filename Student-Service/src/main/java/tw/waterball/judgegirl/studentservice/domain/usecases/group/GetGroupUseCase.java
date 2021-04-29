package tw.waterball.judgegirl.studentservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetGroupUseCase {

    private final GroupRepository groupRepository;

    public void execute(int groupId, Presenter presenter) throws NotFoundException {
        presenter.setGroup(groupRepository.findGroupById(groupId)
                .orElseThrow(() -> notFound(Group.class).id(groupId)));
    }

    public interface Presenter {
        void setGroup(Group group);
    }

}
