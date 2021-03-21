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
        groupRepository.deleteGroupById(groupId);
    }

}
