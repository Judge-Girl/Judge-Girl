package tw.waterball.judgegirl.academy.domain.repositories;

import tw.waterball.judgegirl.primitives.exam.Group;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GroupRepository {

    boolean existsByName(String name);

    Group save(Group group);

    Set<Group> getOwnGroups(int memberId);

    void deleteAll();

    Optional<Group> findGroupById(int groupId);

    boolean existsById(int id);

    List<Group> findAllGroups();

    void deleteGroupById(int groupId);

    List<Group> findGroupsByNames(Iterable<String> names);
}
