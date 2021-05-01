package tw.waterball.judgegirl.examservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.entities.exam.Group;
import tw.waterball.judgegirl.examservice.domain.repositories.GroupRepository;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@AllArgsConstructor
public abstract class AbstractGroupUseCase {
    protected GroupRepository groupRepository;

    protected Group findGroup(int groupId) {
        return groupRepository.findGroupById(groupId)
                .orElseThrow(() -> notFound(Group.class).id(groupId));
    }
}
