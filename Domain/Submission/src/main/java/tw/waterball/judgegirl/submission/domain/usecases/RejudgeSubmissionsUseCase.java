package tw.waterball.judgegirl.submission.domain.usecases;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.submission.domain.usecases.query.SubmissionQueryParams;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - c11037at@gmail.com (Snowmancc)
 */
@Named
@AllArgsConstructor
public class RejudgeSubmissionsUseCase {
    private final SubmissionRepository submissionRepository;
    private final SubmitCodeUseCase submitCodeUseCase;

    public void execute(String submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> notFound(Submission.class).id(submissionId));
        submitCodeUseCase.execute(reSubmission(submission));
    }

    public void execute(SubmissionQueryParams queryParams) {
        submissionRepository.query(queryParams).stream()
                .map(this::reSubmission).forEach(submitCodeUseCase::execute);
    }

    private Submission reSubmission(Submission submission) {
        Submission newSubmission = new Submission(submission.getStudentId(),
                submission.getProblemId(), submission.getLanguageEnvName(), submission.getSubmittedCodesFileId());
        newSubmission.setBag(submission.getBag());
        return newSubmission;
    }
}
