package tw.waterball.judgegirl.examservice.repositories;

import tw.waterball.judgegirl.entities.Question;

public interface QuestionRepository {

    Question save(Question question);

    void deleteAll();
}