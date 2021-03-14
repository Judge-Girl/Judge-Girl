package tw.waterball.judgegirl.studentservice.domain.repositories;

import tw.waterball.judgegirl.entities.Student;

import java.util.Optional;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
public interface StudentRepository {
    Optional<Student> findByEmailAndPassword(String email, String pwd);

    Optional<Student> findByEmail(String email);

    Optional<Student> findStudentById(Integer id);

    boolean existsByEmail(String email);

    Student save(Student student);

    void deleteAll();
}
