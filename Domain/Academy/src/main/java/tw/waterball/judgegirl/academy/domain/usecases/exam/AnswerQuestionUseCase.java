package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.academy.domain.usecases.VerdictIssuedEventHandler;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.exam.*;
import tw.waterball.judgegirl.primitives.grading.Grade;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottlingException;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.SubmitCodeRequest;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.OptionalInt;

import static java.util.Collections.singletonMap;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.ComparableUtils.betterAndNewer;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class AnswerQuestionUseCase implements VerdictIssuedEventHandler {
    public static final String BAG_KEY_EXAM_ID = "examId";
    private final SubmissionServiceDriver submissionService;
    private final ExamRepository examRepository;

    public void execute(Request request, Presenter presenter) throws SubmissionThrottlingException, ExamHasNotBeenStartedOrHasBeenClosedException, NoSubmissionQuotaException {
        Date answerTime = new Date();
        Exam exam = findExam(request.examId);
        Question question = findQuestion(request, exam);

        onlyExamineeCanAnswerQuestion(request);
        examMustHaveBeenStarted(exam);
        int remainingSubmissionQuota = calculateRemainingSubmissionQuota(request, question);
        mustHaveRemainingSubmissionQuota(remainingSubmissionQuota, question.getQuota());

        var submission = submissionService.submit(submitCodeRequest(request));
        Answer answer = answer(request, question, submission, answerTime);
        answer = examRepository.saveAnswer(answer);

        presenter.showRemainingSubmissionQuota(remainingSubmissionQuota);
        presenter.showAnswer(answer, submission);
    }

    private void onlyExamineeCanAnswerQuestion(Request request) throws ExamineeOnlyOperationException {
        if (!examRepository.isExaminee(request.studentId, request.examId)) {
            throw new ExamineeOnlyOperationException();
        }
    }

    private void examMustHaveBeenStarted(Exam exam) throws ExamHasNotBeenStartedOrHasBeenClosedException {
        if (!exam.isOngoing()) {
            throw new ExamHasNotBeenStartedOrHasBeenClosedException(exam);
        }
    }

    private int calculateRemainingSubmissionQuota(Request request, Question question) {
        int answerCount = examRepository.countAnswersInQuestion(question.getId(), request.studentId);
        return question.getQuota() - answerCount;
    }

    private void mustHaveRemainingSubmissionQuota(int remainingSubmissionQuota, int maxSubmissionQuota) throws NoSubmissionQuotaException {
        if (remainingSubmissionQuota <= 0) {
            throw new NoSubmissionQuotaException(maxSubmissionQuota);
        }
    }

    private Exam findExam(int examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> notFound(Exam.class).id(examId));
    }

    private Question findQuestion(Request request, Exam exam) {
        Question.Id id = new Question.Id(request.examId, request.problemId);
        return exam.getQuestionByProblemId(request.problemId)
                .orElseThrow(() -> notFound(Question.class).id(id));
    }

    private SubmitCodeRequest submitCodeRequest(Request request) {
        return new SubmitCodeRequest(request.problemId,
                request.langEnvName, request.studentId, request.fileResources,
                new Bag(singletonMap(BAG_KEY_EXAM_ID, String.valueOf(request.examId))));
    }

    private Answer answer(Request request, Question question, SubmissionView submissionView, Date answerTime) {
        return new Answer(new Answer.Id(question.getId(), request.studentId), submissionView.id, answerTime);
    }

    @Override
    public void handle(VerdictIssuedEvent event) {
        getExamIdFromBag(event.getSubmissionBag())
                .ifPresent(examId -> updateBestRecord(event, examId));
    }

    private OptionalInt getExamIdFromBag(Bag submissionBag) {
        return submissionBag.getAsInteger(BAG_KEY_EXAM_ID);
    }

    private void updateBestRecord(VerdictIssuedEvent event, int examId) {
        Record record = record(event, examId);
        Record bestRecord = examRepository.findRecordOfQuestion(record.getQuestionId(), event.getStudentId())
                .map(currentBest -> betterAndNewer(currentBest, record))
                .orElse(record);

        examRepository.saveRecordOfQuestion(bestRecord);
    }

    private Record record(VerdictIssuedEvent event, int examId) {
        Verdict newVerdict = event.getVerdict();
        return new Record(new Question.Id(examId, event.getProblemId()),
                event.getStudentId(), event.getSubmissionId(), newVerdict.getSummaryStatus(), newVerdict.getMaximumRuntime(), newVerdict.getMaximumMemoryUsage(),
                new Grade(newVerdict.getGrade(), newVerdict.getMaxGrade()), event.getSubmissionTime());
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
        void showRemainingSubmissionQuota(int remainingSubmissionQuota);

        void showAnswer(Answer answer, SubmissionView submission);
    }

}
