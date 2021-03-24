package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;

import javax.inject.Named;

@Named
@AllArgsConstructor
public class GetExamUseCase {
    private final ExamRepository examRepository;

    public void execute(Request request, Presenter presenter) {
        Exam exam = examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
        presenter.setExam(exam);
    }

    public interface Presenter {
        void setExam(Exam exam);
    }

    @Data
    @AllArgsConstructor
    public static class Request {
        public int examId;
    }
}
