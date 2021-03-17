package tw.waterball.judgegirl.springboot.exam.repositories;

import org.springframework.context.annotation.Primary;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tw.waterball.judgegirl.springboot.exam.Exam;

import java.util.Collection;
import java.util.List;

@Primary
@Repository
public interface ExamRepository extends CrudRepository<Exam, Integer> {
    List<Exam> findByIdIn(Collection<Integer> examIds);
}