package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.impl.CascadeGroupDeletion;
import tw.waterball.judgegirl.springboot.exam.repositories.jpa.impl.GetGroupsOwnByMember;

/**
 * @author - wally55077@gmail.com
 * @author - johnny850807@gmail.com (Waterball)
 */
@Repository
public interface GroupDAO extends JpaRepository<GroupData, Integer>,
        CascadeGroupDeletion, GetGroupsOwnByMember {

    boolean existsByName(String name);
}
