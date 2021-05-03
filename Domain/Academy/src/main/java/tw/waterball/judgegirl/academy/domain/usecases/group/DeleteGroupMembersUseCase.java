package tw.waterball.judgegirl.academy.domain.usecases.group;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.exam.Group;
import tw.waterball.judgegirl.primitives.exam.MemberId;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToSet;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class DeleteGroupMembersUseCase extends AbstractGroupUseCase {

    public DeleteGroupMembersUseCase(GroupRepository groupRepository) {
        super(groupRepository);
    }

    public void execute(Request request) throws NotFoundException {
        int groupId = request.groupId;
        List<Integer> memberIds = request.memberIds;
        Group group = findGroup(groupId);
        group.deleteMembers(mapToSet(memberIds, MemberId::new));
        groupRepository.save(group);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int groupId;
        public List<Integer> memberIds = new ArrayList<>();
    }

}
