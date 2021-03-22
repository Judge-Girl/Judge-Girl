package tw.waterball.judgegirl.examservice.repositories;

import tw.waterball.judgegirl.entities.Question;

public interface QuestionRepository {

    int deleteByIdAndExamId(Integer questionId, Integer examId);

    Question save(Question question);

    void deleteAll();
}