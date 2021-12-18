package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.ExamineeOnlyOperationException;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetExamUseCase extends AbstractExamUseCase {

    public GetExamUseCase(ExamRepository examRepository) {
        super(examRepository);
    }

    public void execute(Request request, ExamPresenter presenter) throws ExamineeOnlyOperationException {
        Exam exam = findExam(request.examId);
        onlyExamineeCanAccessTheExam(request, exam);
        presenter.showExam(exam);
    }

    private void onlyExamineeCanAccessTheExam(Request request, Exam exam) {
        if (request.onlyExamineeCanAccess && !exam.hasExaminee(request.studentId)) {
            throw new ExamineeOnlyOperationException();
        }
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
