package tw.waterball.judgegirl.academy.domain.usecases.group;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class DeleteGroupUseCase {

    private final GroupRepository groupRepository;

    public void execute(int groupId) throws NotFoundException {
        groupRepository.deleteGroupById(groupId);
    }

}
