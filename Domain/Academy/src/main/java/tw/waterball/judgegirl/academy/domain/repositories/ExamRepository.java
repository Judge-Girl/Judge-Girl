package tw.waterball.judgegirl.academy.domain.repositories;

import tw.waterball.judgegirl.primitives.exam.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExamRepository {

    Optional<Exam> findById(int examId);

    Optional<Question> findQuestion(Question.Id id);

    List<Question> findQuestionsInExam(int examId);

    List<Exam> findByIdIn(Collection<Integer> examIds);

    List<Exam> findExams(ExamFilter examFilter);

    void saveRecordOfQuestion(Record record);

    Optional<Record> findRecordOfQuestion(Question.Id questionId, int studentId);

    List<Record> findAllRecordsInAnExam(int examId);

    void addQuestion(Question question);

    void addExaminee(int examId, int studentId);

    void deleteExaminee(Examinee.Id id);

    void addExaminees(int examId, List<Integer> studentIds);

    void deleteExaminees(int examId, List<Integer> studentIds);

    void deleteQuestionById(Question.Id id);

    Exam save(Exam exam);

    void deleteAll();

    Answer saveAnswer(Answer answer);

    Optional<Answer> findAnswer(Answer.Id id);

    int countAnswersInQuestion(Question.Id id, int studentId);

    boolean isExaminee(int studentId, int examId);

    void deleteExamById(int examId);
}