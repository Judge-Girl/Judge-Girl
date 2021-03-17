package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaExamParticipationDataPort extends CrudRepository<ExamParticipationData, Integer> {
    List<ExamParticipationData> findByStudentId(int studentId);
}
