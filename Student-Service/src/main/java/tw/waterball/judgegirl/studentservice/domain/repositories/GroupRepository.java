package tw.waterball.judgegirl.studentservice.domain.repositories;

import tw.waterball.judgegirl.entities.Group;

import java.util.List;

public interface GroupRepository {

    boolean existsByName(String name);

    Group save(Group group);

    void deleteAll();

    Group findGroupById(Integer groupId);

    boolean existsById(Integer id);

    List<Group> findAllGroup();

    void deleteGroupById(Integer groupId);
}
