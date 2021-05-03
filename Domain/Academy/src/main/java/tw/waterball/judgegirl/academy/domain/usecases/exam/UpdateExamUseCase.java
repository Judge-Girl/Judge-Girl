package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;

import javax.inject.Named;
import java.util.Date;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

@Named
public class UpdateExamUseCase {
    private final ExamRepository examRepository;

    public UpdateExamUseCase(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }

    public void execute(Request request, ExamPresenter presenter) throws NotFoundException, IllegalStateException {
        Exam exam = findExam(request);
        updateExam(exam, request);
        presenter.showExam(examRepository.save(exam));
    }

    private Exam findExam(Request request) {
        return examRepository.findById(request.examId)
                .orElseThrow(() -> notFound(Exam.class).id(request.examId));
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
