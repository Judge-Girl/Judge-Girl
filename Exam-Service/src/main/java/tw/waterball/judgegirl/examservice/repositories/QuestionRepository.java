package tw.waterball.judgegirl.examservice.repositories;

import tw.waterball.judgegirl.entities.Question;

public interface QuestionRepository {

    void deleteByIdExamIdAndIdProblemId(int examId, int problemId);

    Question save(Question question);

    void deleteAll();
}