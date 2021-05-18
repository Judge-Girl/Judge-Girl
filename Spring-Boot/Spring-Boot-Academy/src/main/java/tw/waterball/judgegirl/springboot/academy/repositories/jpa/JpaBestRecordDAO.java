package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface JpaBestRecordDAO extends JpaRepository<BestRecordData, BestRecordData.Id> {
    List<BestRecordData> findAllById_ExamId(int examId);
}
