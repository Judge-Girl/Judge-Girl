package tw.waterball.judgegirl.submissionservice.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.submissionservice.domain.usecases.dto.SubmissionQueryParams;

import javax.inject.Named;
import java.util.Collections;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetBestSubmissionUseCase {

    private final SubmissionRepository submissionRepository;

    public void execute(Request request, Presenter presenter)
            throws NotFoundException {
        SubmissionQueryParams params = new SubmissionQueryParams(null,
                request.problemId, request.langEnvName, request.studentId, Collections.emptyMap());
        Submission bestSubmission = submissionRepository.query(params)
                .stream()
                .sorted()
                .reduce((first, second) -> second) // find the last
                .orElseThrow(() -> notFound("Best Submission").message(params));
        presenter.showBestSubmission(bestSubmission);
    }

    public interface Presenter {

        void showBestSubmission(Submission bestSubmission);

    }

    @Value
    @AllArgsConstructor
    public static class Request {
        public Integer problemId;
        public String langEnvName;
        public Integer studentId;
    }

}
