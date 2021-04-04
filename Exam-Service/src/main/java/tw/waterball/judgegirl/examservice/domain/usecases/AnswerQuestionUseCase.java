package tw.waterball.judgegirl.examservice.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.exam.*;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionservice.domain.usecases.SubmitCodeRequest;
import tw.waterball.judgegirl.submissionservice.domain.usecases.exceptions.SubmissionThrottlingException;

import javax.inject.Named;
import java.util.List;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class AnswerQuestionUseCase {
    private final SubmissionServiceDriver submissionServiceDriver;
    private final ExamRepository examRepository;

    public void execute(Request request, Presenter presenter) throws ExamHasNotBeenStartedException, NoSubmissionQuotaException, SubmissionThrottlingException {
        Exam exam = findExam(request);
        Question question = findQuestion(request, exam);

        studentMustParticipateExam(request);
        examMustHaveBeenStarted(exam);
        answerCountMustNotExceedQuota(request, question);

        SubmissionView submissionView = submissionServiceDriver.submit(submitCodeRequest(request));
        Answer answer = new Answer(new Answer.Id(question.getId(), request.studentId), submissionView.id);

        answer = examRepository.saveAnswer(answer);
        presenter.setAnswer(answer);
    }

    private void studentMustParticipateExam(Request request) throws YouAreNotAnExamineeException {
        if (!examRepository.hasStudentParticipatedExam(request.studentId, request.examId)) {
            throw new YouAreNotAnExamineeException();
        }
    }

    private void examMustHaveBeenStarted(Exam exam) throws ExamHasNotBeenStartedException {
        if (!exam.isCurrent()) {
            throw new ExamHasNotBeenStartedException();
        }
    }

    private void answerCountMustNotExceedQuota(Request request, Question question) throws NoSubmissionQuotaException {
        int answerCount = examRepository.countAnswersInQuestion(question.getId(), request.studentId);
        if (answerCount >= question.getQuota()) {
            throw new NoSubmissionQuotaException();
        }
    }

    private Exam findExam(Request request) {
        return examRepository.findById(request.examId)
                .orElseThrow(() -> notFound("exam").id(request.examId));
    }

    private Question findQuestion(Request request, Exam exam) {
        Question.Id id = new Question.Id(request.examId, request.problemId);
        return exam.getQuestionByProblemId(request.problemId)
                .orElseThrow(() -> notFound("question").id(id));
    }

    private SubmitCodeRequest submitCodeRequest(Request request) {
        return new SubmitCodeRequest(request.problemId,
                request.langEnvName, request.studentId, request.fileResources);
    }

    @Value
    public static class Request {
        int examId;
        int problemId;
        String langEnvName;
        int studentId;
        public List<FileResource> fileResources;
    }

    public interface Presenter {
        void setAnswer(Answer answer);
    }
}
