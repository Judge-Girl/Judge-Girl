package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaExamineeDataPort extends JpaRepository<ExamineeData, ExamineeData.Id> {
    boolean existsById_StudentIdAndId_ExamId(int studentId, int examId);

    List<ExamineeData> findByStudentId(int studentId, Pageable pageable);

}
