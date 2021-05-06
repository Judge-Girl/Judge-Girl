package tw.waterball.judgegirl.academy.domain.repositories;

import tw.waterball.judgegirl.primitives.Homework;

import java.util.Optional;

public interface HomeworkRepository {

    Homework save(Homework homework);

    Optional<Homework> findHomeworkById(int homeworkId);

    void deleteAll();
}
