package tw.waterball.judgegirl.academy.domain.usecases.group;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.exam.Group;
import tw.waterball.judgegirl.primitives.exam.MemberId;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class AddGroupMemberUseCase extends AbstractGroupUseCase {
    public AddGroupMemberUseCase(GroupRepository groupRepository) {
        super(groupRepository);
    }

    public void execute(Request request) throws NotFoundException {
        int groupId = request.groupId;
        int studentId = request.studentId;
        Group group = findGroup(groupId);

        if (!hasStudentAddedIntoGroup(group, studentId)) {
            group.addMember(new MemberId(studentId));
            groupRepository.save(group);
        }
    }

    private boolean hasStudentAddedIntoGroup(Group group, int studentId) {
        return group.getMemberIds()
                .stream()
                .map(MemberId::getId)
                .anyMatch(groupStudentId -> studentId == groupStudentId);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int groupId;
        public int studentId;
    }

}
