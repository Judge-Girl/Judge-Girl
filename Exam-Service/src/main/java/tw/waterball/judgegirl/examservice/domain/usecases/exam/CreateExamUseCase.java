package tw.waterball.judgegirl.examservice.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;

import javax.inject.Named;
import java.util.Date;

@Named
public class CreateExamUseCase {
    private final ExamRepository examRepository;

    public CreateExamUseCase(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public void execute(Request request, ExamPresenter presenter) throws IllegalStateException {
        Exam exam = new Exam(request.name, request.startTime, request.endTime, request.description);
        exam.validate();
        presenter.showExam(examRepository.save(exam));
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        public String name;
        public Date startTime;
        public Date endTime;
        public String description;
    }

}