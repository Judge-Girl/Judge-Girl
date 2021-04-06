package tw.waterball.judgegirl.examservice.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;

import javax.inject.Named;
import java.util.Date;

@Named
public class UpdateExamUseCase {
    private final ExamRepository examRepository;

    public UpdateExamUseCase(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public void execute(Request request, Presenter presenter) throws NotFoundException {
        Exam exam = examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
        exam.setName(request.name);
        exam.setStartTime(request.startTime);
        exam.setEndTime(request.endTime);
        exam.setDescription(request.description);
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
        public int examId;
        public String name;
        public Date startTime;
        public Date endTime;
        public String description;
    }

}
