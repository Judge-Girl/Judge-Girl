package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.ExamineeOnlyOperationException;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class GetExamUseCase {
    private final ExamRepository examRepository;

    public void execute(Request request, ExamPresenter presenter) throws ExamineeOnlyOperationException {
        Exam exam = findExam(request.examId);
        onlyExamineeCanAccessTheExam(request, exam);
        presenter.showExam(exam);
    }

    private void onlyExamineeCanAccessTheExam(Request request, Exam exam) {
        if (request.isOnlyExamineeCanAccess() && !exam.hasExaminee(request.studentId)) {
            throw new ExamineeOnlyOperationException();
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
        private boolean onlyExamineeCanAccess;
        private int studentId;
    }

}
