package tw.waterball.judgegirl.springboot.submission.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.waterball.judgegirl.submission.domain.usecases.FindBestRecordUseCase;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@AllArgsConstructor
@RequestMapping("/api/submissions")
@RestController
public class SubmissionQueryController {
    private final FindBestRecordUseCase findBestRecordUseCase;

    @PostMapping(value = "/best", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<SubmissionView> findBestRecord(@RequestBody String submissionIdsSplitByCommas) {
        String[] submissionIds = submissionIdsSplitByCommas.split("\\s*,\\s*");
        if (submissionIds.length == 0) {
            return badRequest().build();
        }
        SubmissionPresenter presenter = new SubmissionPresenter();
        findBestRecordUseCase.execute(new FindBestRecordUseCase.Request(submissionIds), presenter);
        return ok(presenter.present());
    }
}
