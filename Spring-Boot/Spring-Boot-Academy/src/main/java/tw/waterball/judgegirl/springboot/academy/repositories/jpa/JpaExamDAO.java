package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaExamDAO extends JpaRepository<ExamData, Integer>, FilterExams {

    Optional<ExamData> findById(int examId);

    List<ExamData> findByIdIn(Collection<Integer> examIds);

}
