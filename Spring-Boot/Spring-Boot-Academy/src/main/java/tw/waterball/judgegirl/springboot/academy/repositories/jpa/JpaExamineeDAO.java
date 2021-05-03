package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaExamineeDAO extends JpaRepository<ExamineeData, ExamineeData.Id> {
    boolean existsById_StudentIdAndId_ExamId(int studentId, int examId);

    List<ExamineeData> findByStudentId(int studentId, Pageable pageable);

}
