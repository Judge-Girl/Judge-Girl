package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.domain.Pageable;
import tw.waterball.judgegirl.examservice.repositories.ExamFilter;

import java.util.Date;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface FilterExamsPort {
    List<ExamData> findStudentExams(int studentId, ExamFilter.Status status, Date now, Pageable pageable);

    List<ExamData> findExams(ExamFilter.Status status, Date now, Pageable pageable);

}
