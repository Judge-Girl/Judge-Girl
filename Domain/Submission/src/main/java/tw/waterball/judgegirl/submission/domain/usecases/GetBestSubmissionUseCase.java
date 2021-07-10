package tw.waterball.judgegirl.submission.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.submission.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.submission.domain.usecases.query.SubmissionQueryParams;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetBestSubmissionUseCase {

    private final SubmissionRepository submissionRepository;

    public void execute(Request request, Presenter presenter) throws NotFoundException {
        var params = SubmissionQueryParams.query()
                .problemId(request.problemId)
                .languageEnvName(request.langEnvName)
                .studentId(request.studentId).build();
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
