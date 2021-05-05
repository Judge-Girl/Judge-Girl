package tw.waterball.judgegirl.springboot.academy.controllers;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tw.waterball.judgegirl.academy.domain.repositories.ExamFilter;
import tw.waterball.judgegirl.academy.domain.usecases.exam.*;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Answer;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.exam.YouAreNotAnExamineeException;
import tw.waterball.judgegirl.problemapi.views.ProblemView;
import tw.waterball.judgegirl.springboot.academy.presenters.ExamHomePresenter;
import tw.waterball.judgegirl.springboot.academy.presenters.ExamPresenter;
import tw.waterball.judgegirl.springboot.academy.view.AnswerView;
import tw.waterball.judgegirl.springboot.academy.view.ExamHome;
import tw.waterball.judgegirl.springboot.academy.view.ExamView;
import tw.waterball.judgegirl.springboot.academy.view.QuestionView;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;

import java.util.List;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
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
    private final UpdateQuestionUseCase updateQuestionUseCase;
    private final DeleteQuestionUseCase deleteQuestionUseCase;
    private final GetExamProgressOverviewUseCase getExamProgressOverviewUseCase;
    private final AnswerQuestionUseCase answerQuestionUseCase;
    private final UpdateExamUseCase updateExamUseCase;
    private final GetExamUseCase getExamUseCase;
    private final ExamPresenter examPresenter;
    private final GetExamineesUseCase getExamineesUseCase;
    private final AddExamineesUseCase addExamineesUseCase;
    private final AddGroupOfExamineesUseCase addGroupOfExamineesUseCase;
    private final DeleteExamineesUseCase deleteExamineesUseCase;

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
        getExamsUseCase.execute(ExamFilter.studentId(studentId)
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

    @PutMapping("/exams/{examId}/problems/{problemId}")
    public void updateQuestion(@PathVariable int examId, @PathVariable int problemId,
                               @RequestBody UpdateQuestionUseCase.Request request) {
        request.setExamId(examId);
        request.setProblemId(problemId);
        updateQuestionUseCase.execute(request);
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

    @GetMapping("/exams/{examId}/students")
    public List<StudentView> getExaminees(@PathVariable int examId) {
        ExamineesPresenter presenter = new ExamineesPresenter();
        getExamineesUseCase.execute(examId, presenter);
        return presenter.present();
    }

    @PostMapping("/exams/{examId}/students")
    public List<String> addExaminees(@PathVariable int examId, @RequestBody List<String> emails) {
        AddExamineesUseCase.Request request = new AddExamineesUseCase.Request();
        request.emails = emails;
        request.examId = examId;
        AddExamineesPresenter presenter = new AddExamineesPresenter();
        addExamineesUseCase.execute(request, presenter);
        return presenter.present();
    }

    @PostMapping("/exams/{examId}/groups")
    public void addGroupsOfExaminees(@PathVariable int examId,
                                     @RequestBody AddGroupOfExamineesUseCase.Request request) {
        request.examId = examId;
        addGroupOfExamineesUseCase.execute(request);
    }

    @DeleteMapping("/exams/{examId}/students")
    public void deleteExaminees(@PathVariable int examId, @RequestBody List<String> emails) {
        DeleteExamineesUseCase.Request request = new DeleteExamineesUseCase.Request();
        request.emails = emails;
        request.examId = examId;
        deleteExamineesUseCase.execute(request);
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

class ExamineesPresenter implements GetExamineesUseCase.Presenter {
    private List<Student> examinees;

    @Override
    public void showExaminees(List<Student> examinees) {
        this.examinees = examinees;
    }

    public List<StudentView> present() {
        return mapToList(examinees, StudentView::toViewModel);
    }
}

class AddExamineesPresenter implements AddExamineesUseCase.Presenter {
    private List<String> errorList;

    @Override
    public void showNotFoundEmails(List<String> emails) {
        errorList = emails;
    }

    public List<String> present() {
        return errorList;
    }
}