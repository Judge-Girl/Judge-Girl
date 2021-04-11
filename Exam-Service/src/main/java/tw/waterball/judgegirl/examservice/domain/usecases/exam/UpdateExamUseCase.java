package tw.waterball.judgegirl.examservice.domain.usecases.exam;

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

    public void execute(Request request, ExamPresenter presenter) throws NotFoundException, IllegalStateException {
        Exam exam = examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
        updateExam(exam, request);
        presenter.showExam(examRepository.save(exam));
    }

    private void updateExam(Exam exam, Request request) throws IllegalStateException {
        exam.setName(request.name);
        exam.setStartTime(request.startTime);
        exam.setEndTime(request.endTime);
        exam.setDescription(request.description);
        exam.validate();
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
