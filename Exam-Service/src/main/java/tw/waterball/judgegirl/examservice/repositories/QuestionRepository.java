package tw.waterball.judgegirl.examservice.repositories;

import tw.waterball.judgegirl.entities.Question;

public interface QuestionRepository {

    long deleteByIdAndExamId(int questionId, int examId);

    Question save(Question question);

    void deleteAll();
}