package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface JpaExamDataPort extends CrudRepository<ExamData, Integer> {

    List<ExamData> findByIdIn(Collection<Integer> examIds);

}
