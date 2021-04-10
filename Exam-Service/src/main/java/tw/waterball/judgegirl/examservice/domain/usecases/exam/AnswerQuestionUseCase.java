package tw.waterball.judgegirl.examservice.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.exam.*;
import tw.waterball.judgegirl.entities.submission.Bag;
import tw.waterball.judgegirl.entities.submission.verdict.Verdict;
import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.examservice.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.examservice.domain.usecases.VerdictIssuedEventListener;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.SubmitCodeRequest;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import javax.inject.Named;
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
public class AnswerQuestionUseCase implements VerdictIssuedEventListener {
    public static final String BAG_KEY_EXAM_ID = "exam-id";
    private final SubmissionServiceDriver submissionService;
    private final ExamRepository examRepository;

    public void execute(Request request, Presenter presenter) throws ExamHasNotBeenStartedException, NoSubmissionQuotaException {
        Exam exam = findExam(request);
        Question question = findQuestion(request, exam);

        studentMustParticipateExam(request);
        examMustHaveBeenStarted(exam);
        answerCountMustNotExceedQuota(request, question);

        SubmissionView submissionView = submissionService.submit(submitCodeRequest(request));
        Answer answer = answer(request, question, submissionView);
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
                request.langEnvName, request.studentId, request.fileResources,
                new Bag(singletonMap(BAG_KEY_EXAM_ID, String.valueOf(request.examId))));
    }

    private Answer answer(Request request, Question question, SubmissionView submissionView) {
        return new Answer(new Answer.Id(question.getId(), request.studentId), submissionView.id);
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

    @Override
    public void onVerdictIssued(VerdictIssuedEvent event) {
        getExamIdFromBag(event.getSubmissionBag())
                .ifPresent(examId -> handleVerdictIssuedEvent(event, examId));
    }

    private void handleVerdictIssuedEvent(VerdictIssuedEvent event, int examId) {
        Record record = record(event, examId);
        Record bestRecord = examRepository.findBestRecordOfQuestion(record.getQuestionId(), event.getStudentId())
                .map(currentBest -> betterAndNewer(currentBest, record))
                .orElse(record);

        examRepository.saveBestRecordOfQuestion(bestRecord);
    }

    private OptionalInt getExamIdFromBag(Bag submissionBag) {
        return submissionBag.getAsInteger(BAG_KEY_EXAM_ID);
    }

    private Record record(VerdictIssuedEvent event, int examId) {
        Verdict newVerdict = event.getVerdict();
        return new Record(new Question.Id(examId, event.getProblemId()),
                event.getStudentId(), newVerdict.getSummaryStatus(), newVerdict.getMaximumRuntime(), newVerdict.getMaximumMemoryUsage(),
                newVerdict.getTotalGrade());
    }

}
