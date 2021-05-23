package tw.waterball.judgegirl.springboot.submission.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.submission.domain.usecases.sample.DowngradeSampleBackToSubmissionUseCase;
import tw.waterball.judgegirl.submission.domain.usecases.sample.GetSamplesUseCase;
import tw.waterball.judgegirl.submission.domain.usecases.sample.UpgradeSubmissionToSampleUseCase;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - wally55077@gmail.com
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class SampleSubmissionController {
    private final TokenService tokenService;
    private final UpgradeSubmissionToSampleUseCase upgradeSubmissionToSampleUseCase;
    private final GetSamplesUseCase getSamplesUseCase;
    private final DowngradeSampleBackToSubmissionUseCase downgradeSampleBackToSubmissionUseCase;

    @GetMapping("/problems/{problemId}/samples")
    public List<SubmissionView> getSamples(@PathVariable int problemId) {
        GetSamplesPresenter presenter = new GetSamplesPresenter();
        getSamplesUseCase.execute(problemId, presenter);
        return presenter.present();
    }

    @PostMapping("/submissions/{submissionId}/sample")
    public void upgradeSubmissionToSample(@RequestHeader("Authorization") String authorization,
                                          @PathVariable String submissionId) {
        tokenService.ifAdminToken(authorization,
                token -> upgradeSubmissionToSampleUseCase.execute(submissionId));
    }

    @DeleteMapping("/submissions/{submissionId}/sample")
    public void downgradeSampleBackToSubmission(@RequestHeader("Authorization") String authorization,
                                                @PathVariable String submissionId) {
        tokenService.ifAdminToken(authorization, token ->
                downgradeSampleBackToSubmissionUseCase.execute(submissionId));
    }

}

class GetSamplesPresenter implements GetSamplesUseCase.Presenter {

    private final List<Submission> sampleSubmissions = new LinkedList<>();

    @Override
    public void showSampleSubmission(Submission sampleSubmission) {
        sampleSubmissions.add(sampleSubmission);
    }

    public List<SubmissionView> present() {
        return sampleSubmissions.stream().map(SubmissionView::toViewModel).collect(Collectors.toList());
    }
}
