package tw.waterball.judgegirl.examservice.repositories;

import tw.waterball.judgegirl.entities.Exam;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExamRepository {

    Optional<Exam> findById(int examId);

    List<Exam> findByIdIn(Collection<Integer> examIds);

    void deleteQuestionById(int examId, int problemId);

    Exam save(Exam exam);

    void deleteAll();
}