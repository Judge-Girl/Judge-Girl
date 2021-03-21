package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateGroupNameException;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class CreateGroupUseCase {

    private final GroupRepository groupRepository;

    public void execute(Request request, Presenter presenter)
            throws DuplicateGroupNameException {
        String groupName = request.name;
        Group group = new Group(groupName);
        group.validate();
        validateGroupName(groupName);
        presenter.setGroup(groupRepository.save(group));
    }

    private void validateGroupName(String name) throws DuplicateGroupNameException {
        if (groupRepository.existsByName(name)) {
            throw new DuplicateGroupNameException();
        }
    }

    public interface Presenter {
        void setGroup(Group group);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private String name;
    }
}
