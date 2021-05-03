package tw.waterball.judgegirl.submission.domain.usecases.sample;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.submission.domain.repositories.SampleRepository;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class DowngradeSampleBackToSubmissionUseCase {

    private final SubmissionRepository submissionRepository;
    private final SampleRepository sampleRepository;

    public void execute(String submissionId) throws NotFoundException {
        submissionRepository.findById(submissionId)
                .ifPresent(this::downgradeSampleBackToSubmission);
    }

    private void downgradeSampleBackToSubmission(Submission submission) {
        sampleRepository.downgradeSamplesBackToSubmissions(submission.getProblemId(), submission.getId());
    }

}
