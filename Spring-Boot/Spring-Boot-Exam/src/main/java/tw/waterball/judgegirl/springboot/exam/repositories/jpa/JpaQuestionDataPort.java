package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;

@Repository
public interface JpaQuestionDataPort extends CrudRepository<QuestionData, Integer> {
    int deleteByIdExamIdAndIdProblemId(int examId, int problemId);
}
