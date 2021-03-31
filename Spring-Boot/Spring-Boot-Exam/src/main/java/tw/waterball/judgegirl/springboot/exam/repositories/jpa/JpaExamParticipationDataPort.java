package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaExamParticipationDataPort extends JpaRepository<ExamParticipationData, Integer> {
    List<ExamParticipationData> findByStudentId(int studentId, Pageable pageable);
    

}
