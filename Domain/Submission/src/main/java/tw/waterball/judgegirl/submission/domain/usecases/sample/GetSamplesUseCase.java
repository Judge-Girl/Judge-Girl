package tw.waterball.judgegirl.submission.domain.usecases.sample;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.submission.domain.repositories.SampleRepository;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;

import javax.inject.Named;
import java.util.Optional;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetSamplesUseCase {

    private final SampleRepository sampleRepository;
    private final SubmissionRepository submissionRepository;

    public void execute(int problemId, Presenter presenter) {
        sampleRepository.findSampleSubmissionIds(problemId)
                .stream()
                .flatMap(submissionId -> findSubmission(submissionId).stream())
                .forEach(presenter::showSampleSubmission);
    }

    private Optional<Submission> findSubmission(String submissionId) {
        return submissionRepository.findById(submissionId);
    }

    public interface Presenter {

        void showSampleSubmission(Submission sampleSubmission);

    }

}
