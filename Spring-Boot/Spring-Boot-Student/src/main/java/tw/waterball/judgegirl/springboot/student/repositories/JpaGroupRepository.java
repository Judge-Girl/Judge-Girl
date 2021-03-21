package tw.waterball.judgegirl.springboot.student.repositories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupData;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.JpaGroupDataDataPort;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import static tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupData.toData;

/**
 * @author - wally55077@gmail.com
 */
@Component
@AllArgsConstructor
public class JpaGroupRepository implements GroupRepository {

    private final JpaGroupDataDataPort jpaGroupDataDataPort;

    @Override
    public boolean existsByName(String name) {
        return jpaGroupDataDataPort.existsByName(name);
    }

    @Override
    public Group save(Group group) {
        GroupData groupData = jpaGroupDataDataPort.save(toData(group));
        group.setId(groupData.getId());
        return group;
    }
}
