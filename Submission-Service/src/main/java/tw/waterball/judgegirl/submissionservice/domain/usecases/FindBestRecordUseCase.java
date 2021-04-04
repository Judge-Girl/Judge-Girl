package tw.waterball.judgegirl.submissionservice.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;

import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @NotNull
    private List<Submission> findSubmissions(Request request) {
        return Arrays.stream(request.submissionIds)
                .map(submissionRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        String[] submissionIds;
    }

}
