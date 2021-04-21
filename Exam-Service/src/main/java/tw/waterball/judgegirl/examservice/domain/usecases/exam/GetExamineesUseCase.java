package tw.waterball.judgegirl.examservice.domain.usecases.exam;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.entities.exam.Examinee;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;

import javax.inject.Named;
import java.util.List;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class GetExamineesUseCase {
    private final ExamRepository examRepository;
    private final StudentServiceDriver studentServiceDriver;

    public void execute(int examId, Presenter presenter) throws IllegalStateException {
        Exam exam = findExam(examId);
        List<Student> students = findExaminees(exam);
        presenter.showExaminees(students);
    }

    private List<Student> findExaminees(Exam exam) {
        return studentServiceDriver.getStudentsByIds(mapToList(exam.getExaminees(), Examinee::getStudentId));
    }

    private Exam findExam(int examId) {
        return examRepository.findById(examId).orElseThrow(() -> notFound("exam").id(examId));
    }

    public interface Presenter {
        void showExaminees(List<Student> examinees);
    }
}
