package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class DeleteGroupByIdUseCase {

    private final GroupRepository groupRepository;

    public void execute(Integer groupId) throws NotFoundException {
        validateGroupIdExist(groupId);
        groupRepository.deleteGroupById(groupId);
    }

    private void validateGroupIdExist(Integer id) throws NotFoundException {
        if (!groupRepository.existsById(id)) {
            throw new NotFoundException();
        }
    }

}
