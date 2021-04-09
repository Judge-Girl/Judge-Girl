package tw.waterball.judgegirl.examservice.domain.repositories;

import tw.waterball.judgegirl.entities.exam.Answer;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.entities.exam.Record;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ExamRepository {

    Optional<Exam> findById(int examId);

    Optional<Question> findQuestion(Question.Id id);

    List<Question> findQuestionsInExam(int examId);

    List<Exam> findByIdIn(Collection<Integer> examIds);

    List<Exam> findExams(ExamFilter examFilter);

    void saveBestRecordOfQuestion(Record record);

    Optional<Record> findBestRecordOfQuestion(Question.Id questionId, int studentId);

    void addQuestion(Question question);

    void addParticipation(int examId, int studentId);

    void deleteQuestionById(Question.Id id);

    Exam save(Exam exam);

    void deleteAll();

    Answer saveAnswer(Answer answer);

    Optional<Answer> findAnswer(Answer.Id id);

    int countAnswersInQuestion(Question.Id id, int studentId);

    boolean hasStudentParticipatedExam(int studentId, int examId);
}