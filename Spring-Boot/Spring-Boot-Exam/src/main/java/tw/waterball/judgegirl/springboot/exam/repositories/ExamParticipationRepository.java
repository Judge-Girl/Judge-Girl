package tw.waterball.judgegirl.springboot.exam.repositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tw.waterball.judgegirl.springboot.exam.ExamParticipation;

import java.util.List;

@Repository
public interface ExamParticipationRepository extends CrudRepository<ExamParticipation, Integer> {
    List<ExamParticipation> findByStudentId(int studentId);
}
