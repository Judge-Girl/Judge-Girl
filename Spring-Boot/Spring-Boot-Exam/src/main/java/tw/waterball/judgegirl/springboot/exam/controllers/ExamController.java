package tw.waterball.judgegirl.springboot.exam.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.examservice.usecases.CreateExamUseCase;
import tw.waterball.judgegirl.examservice.usecases.GetUpcomingExamUseCase;
import tw.waterball.judgegirl.springboot.exam.view.ExamView;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@AllArgsConstructor
@RestController
public class ExamController {
    private final CreateExamUseCase createExamUseCase;
    private final GetUpcomingExamUseCase getUpcomingExamUseCase;

    @PostMapping("/api/exams")
    public ExamView createExam(@Valid @RequestBody CreateExamUseCase.Request request) {
        CreateExamPresenter presenter = new CreateExamPresenter();
        createExamUseCase.execute(request, presenter);
        return presenter.present();
    }

    @GetMapping("/api/students/{studentId}/exams")
    public List<ExamView> getExams(@PathVariable Integer studentId, @RequestParam String type) {
        if (type.equals("upcoming")) {
            GetUpcomingExamPresenter presenter = new GetUpcomingExamPresenter();
            getUpcomingExamUseCase.execute(new GetUpcomingExamUseCase.Request(studentId), presenter);
            return presenter.present();
        } else {
            throw new IllegalArgumentException("Type: " + type + " not supported.");
        }
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<?> errorHandler(Exception err) {
        return ResponseEntity.badRequest().body(err.getMessage());
    }
}

class CreateExamPresenter implements CreateExamUseCase.Presenter {
    private Exam exam;

    @Override
    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public ExamView present() {
        return ExamView.toViewModel(exam);
    }
}

class GetUpcomingExamPresenter implements GetUpcomingExamUseCase.Presenter {
    private List<Exam> exams;

    @Override
    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    public List<ExamView> present() {
        return exams.stream().map(ExamView::toViewModel).collect(Collectors.toList());
    }
}