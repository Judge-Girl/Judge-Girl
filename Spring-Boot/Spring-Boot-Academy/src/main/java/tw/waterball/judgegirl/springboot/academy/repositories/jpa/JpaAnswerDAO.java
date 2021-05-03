package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface JpaAnswerDAO extends JpaRepository<AnswerData, AnswerData.Id> {
    int countAllByExamIdAndProblemIdAndStudentId(int examId, int problemId, int studentId);
}
