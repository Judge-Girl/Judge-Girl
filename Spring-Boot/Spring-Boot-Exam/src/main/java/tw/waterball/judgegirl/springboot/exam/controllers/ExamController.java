package tw.waterball.judgegirl.springboot.exam.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.exam.Answer;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.entities.exam.YouAreNotAnExamineeException;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamFilter;
import tw.waterball.judgegirl.examservice.domain.usecases.exam.*;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.exam.presenters.ExamHomePresenter;
import tw.waterball.judgegirl.springboot.exam.presenters.ExamPresenter;
import tw.waterball.judgegirl.springboot.exam.view.AnswerView;
import tw.waterball.judgegirl.springboot.exam.view.ExamHome;
import tw.waterball.judgegirl.springboot.exam.view.ExamView;
import tw.waterball.judgegirl.springboot.exam.view.QuestionView;

import java.util.List;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.springboot.utils.MultipartFileUtils.convertMultipartFilesToFileResources;
import static tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient.SUBMIT_CODE_MULTIPART_KEY_NAME;

@CrossOrigin
@Slf4j
@RequestMapping("/api")
@AllArgsConstructor
@RestController
public class ExamController {
    private final CreateExamUseCase createExamUseCase;
    private final GetExamsUseCase getExamsUseCase;
    private final CreateQuestionUseCase createQuestionUseCase;
    private final DeleteQuestionUseCase deleteQuestionUseCase;
    private final GetExamProgressOverviewUseCase getExamProgressOverviewUseCase;
    private final AnswerQuestionUseCase answerQuestionUseCase;
    private final UpdateExamUseCase updateExamUseCase;
    private final GetExamUseCase getExamUseCase;
    private final ExamPresenter examPresenter;
    private final AddExamParticipationUseCase addExamParticipationUseCase;
    private final DeleteExamParticipationUseCase deleteExamParticipationUseCase;

    @PostMapping("/exams")
    public ExamView createExam(@RequestBody CreateExamUseCase.Request request) {
        createExamUseCase.execute(request, examPresenter);
        return examPresenter.present();
    }

    @PutMapping("/exams/{examId}")
    public ExamView updateExam(@PathVariable int examId, @RequestBody UpdateExamUseCase.Request request) {
        request.setExamId(examId);
        updateExamUseCase.execute(request, examPresenter);
        return examPresenter.present();
    }

    @GetMapping("/exams")
    public List<ExamView> getAllExams(@RequestParam(defaultValue = "0", required = false) int skip,
                                      @RequestParam(defaultValue = "50", required = false) int size,
                                      @RequestParam(defaultValue = "all", required = false) ExamFilter.Status status) {
        GetExamsPresenter presenter = new GetExamsPresenter();
        getExamsUseCase.execute(ExamFilter.builder()
                .skip(skip).size(size).status(status).build(), presenter);
        return presenter.present();
    }

    @PostMapping("/exams/{examId}/problems/{problemId}/{langEnvName}/students/{studentId}/answers")
    public AnswerView answerQuestion(@PathVariable int examId,
                                     @PathVariable int problemId,
                                     @PathVariable String langEnvName,
                                     @PathVariable int studentId,
                                     @RequestParam(SUBMIT_CODE_MULTIPART_KEY_NAME) MultipartFile[] submittedCodes) {
        AnswerQuestionPresenter presenter = new AnswerQuestionPresenter();
        List<FileResource> fileResources = convertMultipartFilesToFileResources(submittedCodes);
        answerQuestionUseCase.execute(new AnswerQuestionUseCase.Request(examId, problemId,
                langEnvName, studentId, fileResources), presenter);
        return presenter.present();
    }

    @GetMapping("/students/{studentId}/exams")
    public List<ExamView> getStudentExams(@PathVariable int studentId,
                                          @RequestParam(defaultValue = "0", required = false) int skip,
                                          @RequestParam(defaultValue = "50", required = false) int size,
                                          @RequestParam(defaultValue = "all", required = false) ExamFilter.Status status) {
        GetExamsPresenter presenter = new GetExamsPresenter();
        getExamsUseCase.execute(
                ExamFilter.studentId(studentId)
                        .skip(skip).size(size)
                        .status(status).build(), presenter);
        return presenter.present();
    }

    @PostMapping("/exams/{examId}/problems/{problemId}")
    public QuestionView createQuestion(@PathVariable int examId, @PathVariable int problemId,
                                       @RequestBody CreateQuestionUseCase.Request request) {
        request.setProblemId(problemId);
        request.setExamId(examId);
        CreateQuestionPresenter presenter = new CreateQuestionPresenter();
        createQuestionUseCase.execute(request, presenter);
        return presenter.present();
    }

    @DeleteMapping("/exams/{examId}/problems/{problemId}")
    public void deleteQuestion(@PathVariable int examId, @PathVariable int problemId) {
        deleteQuestionUseCase.execute(new DeleteQuestionUseCase.Request(examId, problemId));
    }


    @GetMapping("/exams/{examId}")
    public ExamView getExamById(@PathVariable int examId) {
        getExamUseCase.execute(examId, examPresenter);
        return examPresenter.present();
    }

    @GetMapping("/exams/{examId}/students/{studentId}/overview")
    public ExamHome getExamOverview(@PathVariable int examId,
                                    @PathVariable int studentId) {
        ExamHomePresenter presenter = new ExamHomePresenter();
        getExamProgressOverviewUseCase.execute(new GetExamProgressOverviewUseCase.Request(examId, studentId), presenter);
        return presenter.present();
    }

    @PostMapping("/exams/{examId}/students")
    public List<String> addExamParticipation(@PathVariable int examId, @RequestBody AddExamParticipationUseCase.Request request) {
        request.examId = examId;
        AddExamParticipationPresenter presenter = new AddExamParticipationPresenter();
        addExamParticipationUseCase.execute(request, presenter);
        return presenter.present();
    }

    @DeleteMapping("/exams/{examId}/students")
    public void deleteExamParticipation(@PathVariable int examId, @RequestBody DeleteExamParticipationUseCase.Request request) {
        request.examId = examId;
        deleteExamParticipationUseCase.execute(request);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({YouAreNotAnExamineeException.class})
    public void handleYouAreNotAnExamineeException() {
    }
}

class AnswerQuestionPresenter implements AnswerQuestionUseCase.Presenter {
    private Answer answer;

    @Override
    public void showAnswer(Answer answer) {
        this.answer = answer;
    }

    public AnswerView present() {
        return AnswerView.toViewModel(answer);
    }
}


class GetExamsPresenter implements GetExamsUseCase.Presenter {
    private List<Exam> exams;

    @Override
    public void showExams(List<Exam> exams) {
        this.exams = exams;
    }

    public List<ExamView> present() {
        return exams.stream().map(ExamView::toViewModel).collect(Collectors.toList());
    }
}

class CreateQuestionPresenter implements CreateQuestionUseCase.Presenter {
    private Question question;

    @Override
    public void setProblem(ProblemView problemView) {
    }

    @Override
    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionView present() {
        return QuestionView.toViewModel(question);
    }
}

class AddExamParticipationPresenter implements AddExamParticipationUseCase.Presenter {
    private List<String> errorList;

    @Override
    public void setErrorEmails(List<String> emails) {
        errorList = emails;
    }

    public List<String> present() {
        return errorList;
    }
}