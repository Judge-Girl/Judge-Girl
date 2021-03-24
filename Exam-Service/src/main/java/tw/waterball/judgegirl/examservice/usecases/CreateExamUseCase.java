package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;

import javax.inject.Named;
import java.util.Date;

@Named
public class CreateExamUseCase {
    private final ExamRepository examRepository;

    public CreateExamUseCase(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public void execute(Request request, Presenter presenter) {
        Exam exam = new Exam(request.name, request.startTime, request.endTime);
        exam.validate();
        presenter.setExam(examRepository.save(exam));
    }

    public interface Presenter {
        void setExam(Exam exam);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        public String name;
        public Date startTime;
        public Date endTime;
    }

}
