package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.commons.exceptions.ForbiddenAccessException;
import tw.waterball.judgegirl.primitives.exam.Exam;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class GetExamUseCase {
    private final ExamRepository examRepository;

    public void execute(Request request, ExamPresenter presenter) {
        Exam exam = findExam(request.examId);
        onlyExamineeCanAccessTheExam(request, exam);
        presenter.showExam(exam);
    }

    private void onlyExamineeCanAccessTheExam(Request request, Exam exam) {
        if (!exam.hasExaminee(request.studentId)) {
            throw new ForbiddenAccessException("Cannot access the exam.");
        }
    }

    private Exam findExam(int examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> notFound(Exam.class).id(examId));
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        private int examId;
        private int studentId;
    }

}
