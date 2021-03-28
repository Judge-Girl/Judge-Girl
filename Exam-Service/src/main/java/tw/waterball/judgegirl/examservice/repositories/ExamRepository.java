package tw.waterball.judgegirl.examservice.repositories;

import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.Question;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExamRepository {

    Optional<Exam> findById(int examId);

    List<Question> findQuestionsInExam(int examId);

    List<Exam> findByIdIn(Collection<Integer> examIds);

    void addQuestion(Question question);

    void deleteQuestionById(Question.Id id);

    Exam save(Exam exam);

    void deleteAll();
}