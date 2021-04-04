package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.exam.Answer;
import tw.waterball.judgegirl.entities.exam.NoSubmissionQuotaException;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionservice.domain.usecases.SubmitCodeRequest;
import tw.waterball.judgegirl.submissionservice.domain.usecases.exceptions.SubmissionThrottlingException;

import javax.inject.Named;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class AnswerQuestionUseCase {
    private final SubmissionServiceDriver submissionServiceDriver;
    private final ExamRepository examRepository;

    public void execute(Request request, Presenter presenter) throws NoSubmissionQuotaException, SubmissionThrottlingException {
        Question question = findQuestion(request);
        int answerCount = examRepository.countAnswersInQuestion(question.getId(), request.studentId);
        if (answerCount >= question.getQuota()) {
            throw new NoSubmissionQuotaException();
        }

        SubmissionView submissionView = submissionServiceDriver.submit(submitCodeRequest(request));
        Answer answer = new Answer(new Answer.Id(question.getId(), request.studentId), submissionView.id);

        answer = examRepository.saveAnswer(answer);
        presenter.setAnswer(answer);
    }

    private Question findQuestion(Request request) {
        Question.Id id = new Question.Id(request.examId, request.problemId);
        return examRepository.findQuestion(id)
                .orElseThrow(() -> NotFoundException.notFound("question").id(id));
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
