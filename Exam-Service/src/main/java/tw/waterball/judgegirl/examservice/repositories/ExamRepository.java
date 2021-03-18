package tw.waterball.judgegirl.examservice.repositories;

import tw.waterball.judgegirl.entities.Exam;

import java.util.Collection;
import java.util.List;

public interface ExamRepository {

    List<Exam> findByIdIn(Collection<Integer> examIds);

    Exam save(Exam exam);

    void deleteAll();
}