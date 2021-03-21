package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetGroupByIdUseCase {

    private final GroupRepository groupRepository;

    public void execute(Integer groupId, Presenter presenter) throws NotFoundException {
        presenter.setGroup(groupRepository.findGroupById(groupId)
                .orElseThrow(NotFoundException::new));
    }

    public interface Presenter {
        void setGroup(Group group);
    }

}
