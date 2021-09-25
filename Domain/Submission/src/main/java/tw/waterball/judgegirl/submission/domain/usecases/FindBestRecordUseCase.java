package tw.waterball.judgegirl.submission.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;

import javax.inject.Named;
import java.util.List;

/**
 * Find a best record submission given a list of submission ids
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
@AllArgsConstructor
public class FindBestRecordUseCase {
    private final SubmissionRepository submissionRepository;

    public void execute(Request request, SubmissionPresenter presenter) {
        List<Submission> submissions = findSubmissions(request);
        Submission bestRecord = submissions.stream()
                .sorted()
                .reduce((first, second) -> second) // find last
                .orElseThrow(() -> new IllegalArgumentException("No submissions found."));
        presenter.setSubmission(bestRecord);
    }

    private List<Submission> findSubmissions(Request request) {
        return submissionRepository.findAllByIds(request.submissionIds);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        String[] submissionIds;
    }

}
