package tw.waterball.judgegirl.springboot.student.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author - wally55077@gmail.com
 */
@Repository
public interface GroupDataRepository extends JpaRepository<GroupData, Integer> {

    boolean existsGroupDataByName(String name);

}
