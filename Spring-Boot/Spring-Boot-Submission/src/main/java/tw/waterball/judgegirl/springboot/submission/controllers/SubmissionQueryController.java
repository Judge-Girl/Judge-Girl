package tw.waterball.judgegirl.springboot.submission.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.springboot.submission.presenters.SubmissionsPresenter;
import tw.waterball.judgegirl.submission.domain.usecases.FindBestRecordUseCase;
import tw.waterball.judgegirl.submission.domain.usecases.GetSubmissionsUseCase;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.List;

import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.ok;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@AllArgsConstructor
@RequestMapping("/api/submissions")
@RestController
public class SubmissionQueryController {
    private final GetSubmissionsUseCase getSubmissionsUseCase;
    private final FindBestRecordUseCase findBestRecordUseCase;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping
    public List<SubmissionView> getSubmissionsByIds(@RequestParam String[] ids) {
        var presenter = new SubmissionsPresenter();
        getSubmissionsUseCase.execute(ids, presenter);
        return presenter.present();
    }

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
