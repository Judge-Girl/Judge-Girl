package tw.waterball.judgegirl.submission.domain.usecases.sample;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.submission.domain.repositories.SampleRepository;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class UpgradeSubmissionToSampleUseCase {

    private final SubmissionRepository submissionRepository;
    private final SampleRepository sampleRepository;

    public void execute(String submissionId) throws NotFoundException {
        int problemId = findSubmission(submissionId).getProblemId();
        sampleRepository.upgradeSubmissionsToSamples(problemId, submissionId);
    }

    private Submission findSubmission(String submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> notFound(Submission.class).id(submissionId));
    }

}
