package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.Value;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.commons.utils.functional.GetById;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Examinee;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.exam.Record;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;

import javax.inject.Named;
import java.util.List;

import static java.util.function.Function.identity;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.*;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class CalculateExamScoreUseCase extends AbstractExamUseCase {
    private final StudentServiceDriver studentServiceDriver;

    public CalculateExamScoreUseCase(ExamRepository examRepository, StudentServiceDriver studentServiceDriver) {
        super(examRepository);
        this.studentServiceDriver = studentServiceDriver;
    }

    public void execute(int examId, Presenter presenter) {
        Exam exam = findExam(examId);
        List<Record> records = examRepository.findAllRecordsInAnExam(examId);

        var examinees = examinees(exam);
        List<ExamineeRecord> examineeRecords = examineeRecords(exam, records, examinees);
        double averageScore = average(examineeRecords, ExamineeRecord::getTotalScore);
        present(presenter, exam, examineeRecords, averageScore);
    }

    private GetById<Integer, Student> examinees(Exam exam) {
        var idToExaminee = toMap(studentServiceDriver.getStudentsByIds(
                mapToList(exam.getExaminees(), Examinee::getStudentId)), Student::getId, identity());
        return idToExaminee::get;
    }

    private List<ExamineeRecord> examineeRecords(Exam exam, List<Record> records, GetById<Integer, Student> examinees) {
        var examineeToQuestionRecords =
                groupingBy(questionRecords(exam, records),
                        questionRecord -> examinees.get(questionRecord.getStudentId()));
        return zipToList(examineeToQuestionRecords, ExamineeRecord::new);
    }

    private List<QuestionRecord> questionRecords(Exam exam, List<Record> records) {
        return mapToList(records, record -> new QuestionRecord(exam.getQuestionById(record.getQuestionId()), record));
    }

    private void present(Presenter presenter, Exam exam, List<ExamineeRecord> examineeRecords, double averageScore) {
        presenter.showExam(exam);
        examineeRecords.forEach(presenter::addRecord);
        presenter.showStatistics(averageScore, exam.getMaxScore());
    }

    public interface Presenter {

        void showExam(Exam exam);

        void addRecord(ExamineeRecord examineeRecord);

        void showStatistics(double averageScore, int maxScore);
    }

    @Value
    public static class ExamineeRecord {
        Student examinee;
        List<QuestionRecord> questionRecords;
        List<Integer> scores;
        int totalScore;

        public ExamineeRecord(Student examinee, List<QuestionRecord> questionRecords) {
            this.examinee = examinee;
            this.questionRecords = questionRecords;
            this.scores = mapToList(questionRecords, QuestionRecord::calculateScore);
            this.totalScore = sum(scores);
        }
    }

    @Value
    public static class QuestionRecord {
        Question question;
        Record record;

        public int calculateScore() {
            return question.calculateScore(record);
        }

        public int getStudentId() {
            return record.getStudentId();
        }
    }
}
