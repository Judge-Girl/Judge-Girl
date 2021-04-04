package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.data.repository.CrudRepository;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface JpaAnswerDataPort extends CrudRepository<AnswerData, AnswerData.Id> {
    int countAllByExamIdAndProblemIdAndStudentId(int examId, int problemId, int studentId);
}
