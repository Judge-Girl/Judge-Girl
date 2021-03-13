package tw.waterball.judgegirl.springboot.student.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Repository
public interface JpaStudentDataPort extends CrudRepository<StudentData, Integer> {
    Optional<StudentData> findByEmailAndPassword(String email, String pwd);

    Optional<StudentData> findByEmail(String email);

    Optional<StudentData> findStudentById(Integer id);

    boolean existsByEmail(String email);
}
