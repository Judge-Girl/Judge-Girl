package tw.waterball.judgegirl.springboot.exam.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.usecases.*;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.exam.view.ExamOverview;
import tw.waterball.judgegirl.springboot.exam.view.ExamView;
import tw.waterball.judgegirl.springboot.exam.view.QuestionView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin
@AllArgsConstructor
@RestController
public class ExamController {
    private final CreateExamUseCase createExamUseCase;
    private final GetUpcomingExamsUseCase getUpcomingExamUseCase;
    private final CreateQuestionUseCase addQuestionUseCase;
    private final DeleteQuestionUseCase deleteQuestionUseCase;
    private final GetExamOverviewUseCase getExamOverviewUseCase;

    @PostMapping("/api/exams")
    public ExamView createExam(@RequestBody CreateExamUseCase.Request request) {
        CreateExamPresenter presenter = new CreateExamPresenter();
        createExamUseCase.execute(request, presenter);
        return presenter.present();
    }

    @GetMapping("/api/students/{studentId}/exams")
    public List<ExamView> getExams(@PathVariable Integer studentId, @RequestParam String type) {
        if (type.equals("upcoming")) {
            GetUpcomingExamPresenter presenter = new GetUpcomingExamPresenter();
            getUpcomingExamUseCase.execute(new GetUpcomingExamsUseCase.Request(studentId), presenter);
            return presenter.present();
        } else {
            throw new IllegalArgumentException("Type: " + type + " not supported.");
        }
    }

    @PostMapping("/api/exams/{examId}/questions")
    public QuestionView createQuestion(@PathVariable int examId, @RequestBody CreateQuestionUseCase.Request request) {
        request.setExamId(examId);
        CreateQuestionPresenter presenter = new CreateQuestionPresenter();
        addQuestionUseCase.execute(request, presenter);
        return presenter.present();
    }

    @DeleteMapping("/api/exams/{examId}/questions/{questionId}")
    public void deleteQuestion(@PathVariable int examId, @PathVariable int questionId) {
        deleteQuestionUseCase.execute(new DeleteQuestionUseCase.Request(examId, questionId));
    }

    @GetMapping("/api/exams/{examId}/overview")
    public ExamOverview getExamOverview(@PathVariable int examId) {
        GetExamOverviewPresenter presenter = new GetExamOverviewPresenter();
        getExamOverviewUseCase.execute(new GetExamOverviewUseCase.Request(examId), presenter);
        return presenter.present();
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class, NotFoundException.class})
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

class GetUpcomingExamPresenter implements GetUpcomingExamsUseCase.Presenter {
    private List<Exam> exams;

    @Override
    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    public List<ExamView> present() {
        return exams.stream().map(ExamView::toViewModel).collect(Collectors.toList());
    }
}

class CreateQuestionPresenter implements CreateQuestionUseCase.Presenter {
    private Question question;

    @Override
    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionView present() {
        return QuestionView.toViewModel(question);
    }
}

class GetExamOverviewPresenter implements GetExamOverviewUseCase.Presenter {

    private Exam exam;

    private List<ProblemView> problemViews = new ArrayList<>();

    @Override
    public void setExam(Exam exam) {
        this.exam = exam;
    }

    @Override
    public void addProblem(ProblemView problemView) {
        problemViews.add(problemView);
    }

    public ExamOverview present() {
        return ExamOverview.toViewModel(exam, problemViews);
    }

}