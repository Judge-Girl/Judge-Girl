package tw.waterball.judgegirl.submissionservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetSubmissionUseCase extends BaseSubmissionUseCase {

    public GetSubmissionUseCase(SubmissionRepository submissionRepository) {
        super(submissionRepository);
    }

    public void execute(Request request, SubmissionPresenter presenter) {
        Submission submission = doFindSubmission(request.studentId, request.submissionId);
        validateRequest(request, submission);
        presenter.setSubmission(submission);
    }

    private void validateRequest(Request request, Submission submission) {
        if (submission.getProblemId() != request.problemId) {
            throw new NotFoundException(submission.getId(), "submission");
        }
        if (!submission.getLanguageEnvName().equals(request.getLanguageEnvName())) {
            throw new NotFoundException(submission.getId(), "submission");
        }
    }

    @Value
    public static class Request {
        public int problemId;
        public String languageEnvName;
        public int studentId;
        public String submissionId;
    }
}
