package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.Value;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.exam.Record;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
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
        Map<Integer, Student> studentMap = studentServiceDriver.getStudentsByIds(
                mapToList(records, Record::getStudentId))
                .stream().collect(toMap(Student::getId, identity()));
        Map<Integer, List<Record>> studentIdToRecords = records.stream().collect(Collectors.groupingBy(Record::getStudentId));
        List<ExamineeRecord> examineeRecords = records.stream().map(record -> {
            List<Record> studentRecords = studentIdToRecords.get(record.getStudentId());
            Map<Question, Record> map = studentRecords.stream().collect(
                    toMap(r -> exam.getQuestionById(r.getQuestionId()).orElseThrow(), identity()));
            return new ExamineeRecord(studentMap.get(record.getStudentId()), map);
        }).collect(toList());
        examineeRecords.forEach(presenter::showEveryRecord);
        double averageScore = examineeRecords.stream().mapToInt(ExamineeRecord::getTotalScore).average().orElse(0);
        int maxScore = sum(exam.getQuestions(), Question::getScore);
        presenter.showExam(exam);
        presenter.showStatistics(averageScore, maxScore);
    }

    public interface Presenter {
        void showExam(Exam exam);

        void showEveryRecord(ExamineeRecord examineeRecord);

        void showStatistics(double averageScore, int maxScore);
    }

    @Value
    public static class ExamineeRecord {
        Student examinee;
        Map<Question, Record> recordSheet;
        List<Integer> scores;
        int totalScore;

        public ExamineeRecord(Student examinee, Map<Question, Record> recordSheet) {
            this.examinee = examinee;
            this.recordSheet = recordSheet;
            this.scores = zipToList(recordSheet.entrySet(), Question::calculateScore);
            this.totalScore = sum(scores);
        }

    }
}
