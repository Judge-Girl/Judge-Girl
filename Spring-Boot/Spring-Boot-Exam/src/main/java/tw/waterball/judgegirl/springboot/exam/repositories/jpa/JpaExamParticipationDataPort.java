package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaExamParticipationDataPort extends JpaRepository<ExamParticipationData, ExamParticipationData.Id> {
    boolean existsById_StudentIdAndId_ExamId(int studentId, int examId);

    List<ExamParticipationData> findByStudentId(int studentId, Pageable pageable);

}
