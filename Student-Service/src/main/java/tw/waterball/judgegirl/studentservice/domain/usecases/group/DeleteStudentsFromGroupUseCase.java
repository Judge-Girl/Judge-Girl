package tw.waterball.judgegirl.studentservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class DeleteStudentsFromGroupUseCase {

    private final GroupRepository groupRepository;

    public void execute(Request request)
            throws NotFoundException {
        // TODO: improve performance, should only perform one SQL query
        int groupId = request.groupId;
        List<Integer> ids = request.ids;
        Group group = groupRepository.findGroupById(groupId)
                .orElseThrow(NotFoundException::new);
        group.deleteStudentByIds(ids);
        groupRepository.save(group);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int groupId;
        public List<Integer> ids = new ArrayList<>();
    }

}
