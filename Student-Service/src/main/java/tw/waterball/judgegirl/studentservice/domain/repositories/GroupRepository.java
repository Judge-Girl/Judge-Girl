package tw.waterball.judgegirl.studentservice.domain.repositories;

import tw.waterball.judgegirl.entities.Group;

public interface GroupRepository {

    boolean existsByName(String name);

    Group save(Group group);

}
