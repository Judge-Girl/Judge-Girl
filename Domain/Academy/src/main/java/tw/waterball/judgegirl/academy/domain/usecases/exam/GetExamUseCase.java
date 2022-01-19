package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.ExamineeOnlyOperationException;
import tw.waterball.judgegirl.primitives.exam.IpAddress;

import javax.inject.Named;

import static tw.waterball.judgegirl.academy.domain.utils.ExamValidationUtil.onlyExamineeWithWhitelistIpCanAccessOngoingExam;

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
        onlyExamineeWithWhitelistIpCanAccessOngoingExam(request.isStudent, request.studentId, new IpAddress(request.ipAddress), exam);
        presenter.showExam(exam);
    }

    @AllArgsConstructor
    public static class Request {
        public int examId;
        public boolean isStudent;
        public int studentId;
        public String ipAddress;
    }

}
