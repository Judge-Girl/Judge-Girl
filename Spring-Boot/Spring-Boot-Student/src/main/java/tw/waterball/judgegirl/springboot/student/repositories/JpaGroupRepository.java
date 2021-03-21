package tw.waterball.judgegirl.springboot.student.repositories;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.GroupData;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.JpaGroupDataPort;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;

import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public Group findGroupById(Integer groupId) {
        return jpaGroupDataPort.findById(groupId)
                .map(groupData -> new Group(groupData.getId(), groupData.getName()))
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public boolean existsById(Integer id) {
        return jpaGroupDataPort.existsById(id);
    }

    @Override
    public List<Group> findAllGroup() {
        return jpaGroupDataPort.findAll().stream()
                .map(groupData -> new Group(groupData.getId(), groupData.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteGroupById(Integer groupId) {
        jpaGroupDataPort.deleteById(groupId);
    }
}
