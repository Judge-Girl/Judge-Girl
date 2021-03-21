package tw.waterball.judgegirl.springboot.student.repositories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupData;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.JpaGroupDataPort;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import static tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupData.toData;

/**
 * @author - wally55077@gmail.com
 */
@Component
@AllArgsConstructor
public class JpaGroupRepository implements GroupRepository {

    private final JpaGroupDataPort jpaGroupDataPort;

    @Override
    public boolean existsByName(String name) {
        return jpaGroupDataPort.existsByName(name);
    }

    @Override
    public Group save(Group group) {
        GroupData groupData = jpaGroupDataPort.save(toData(group));
        group.setId(groupData.getId());
        return group;
    }

    @Override
    public void deleteAll() {
        jpaGroupDataPort.deleteAll();
    }
}
