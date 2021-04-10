package tw.waterball.judgegirl.examservice.domain.repositories;

import tw.waterball.judgegirl.entities.Homework;

import java.util.Optional;

public interface HomeworkRepository {

    Homework save(Homework homework);

    Optional<Homework> findHomeworkById(int homeworkId);

    void deleteAll();
}
