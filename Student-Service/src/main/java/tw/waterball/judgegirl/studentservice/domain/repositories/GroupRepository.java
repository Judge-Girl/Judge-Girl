package tw.waterball.judgegirl.studentservice.domain.repositories;

import tw.waterball.judgegirl.entities.Group;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {

    boolean existsByName(String name);

    Group save(Group group);

    void deleteAll();

    Optional<Group> findGroupById(int groupId);

    boolean existsById(int id);

    List<Group> findAllGroup();

    void deleteGroupById(int groupId);
}
