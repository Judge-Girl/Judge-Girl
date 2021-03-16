package tw.waterball.judgegirl.springboot.student.repositories;

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.JpaStudentDataPort;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.StudentData;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import java.util.Optional;

import static tw.waterball.judgegirl.springboot.student.repositories.jpa.StudentData.toData;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Component
public class JpaStudentRepository implements StudentRepository {
    private final JpaStudentDataPort jpaStudentDataPort;

    public JpaStudentRepository(JpaStudentDataPort jpaStudentDataPort) {
        this.jpaStudentDataPort = jpaStudentDataPort;
    }

    @Override
    public Optional<Student> findByEmailAndPassword(String email, String pwd) {
        return jpaStudentDataPort
                .findByEmailAndPassword(email, pwd)
                .map(StudentData::toEntity);
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        return jpaStudentDataPort
                .findByEmail(email)
                .map(StudentData::toEntity);
    }

    @Override
    public Optional<Student> findStudentById(Integer id) {
        return jpaStudentDataPort
                .findStudentById(id)
                .map(StudentData::toEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaStudentDataPort.existsByEmail(email);
    }

    @Override
    public Student save(Student student) {
        StudentData data = jpaStudentDataPort.save(toData(student));
        student.setId(data.getId());
        return student;
    }

    @Override
    public void deleteAll() {
        jpaStudentDataPort.deleteAll();
    }
}
