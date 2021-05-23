package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.commons.exceptions.ForbiddenAccessException;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Examinee;
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

    public void execute(Request request, Presenter presenter) throws IllegalStateException {
        Exam exam = findExam(request.examId);
        onlyExamineeCanAccessTheExam(request, exam);
        List<Student> examinees = findExaminees(exam);
        presenter.showExaminees(examinees);
    }

    private void onlyExamineeCanAccessTheExam(Request request, Exam exam) {
        if (!exam.hasExaminee(request.studentId)) {
            throw new ForbiddenAccessException("Cannot access the exam.");
        }
    }

    private List<Student> findExaminees(Exam exam) {
        return studentServiceDriver.getStudentsByIds(mapToList(exam.getExaminees(), Examinee::getStudentId));
    }

    private Exam findExam(int examId) {
        return examRepository.findById(examId).orElseThrow(() -> notFound(Exam.class).id(examId));
    }

    public interface Presenter {
        void showExaminees(List<Student> examinees);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private int examId;
        private int studentId;
    }
}
