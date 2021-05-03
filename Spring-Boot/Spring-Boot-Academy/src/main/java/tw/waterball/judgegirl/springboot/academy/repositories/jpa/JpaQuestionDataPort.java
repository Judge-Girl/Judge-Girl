package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaQuestionDataPort extends JpaRepository<QuestionData, QuestionData.Id> {
    List<QuestionData> findById_ExamId(int examId);
}
