package tw.waterball.judgegirl.springboot.submission.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.submission.domain.usecases.RejudgeSubmissionsUseCase;
import tw.waterball.judgegirl.submission.domain.usecases.query.SubmissionQueryParams;

/**
 * @author - c11037at@gmail.com (snowmancc)
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/submissions")
public class SubmissionRejudgeController {

    private final RejudgeSubmissionsUseCase rejudgeSubmissionsUseCase;

    @PostMapping("/judges")
    void rejudgeSubmissions(@RequestBody Request request) {
        var submissionQueryParams = SubmissionQueryParams.builder()
                .problemId(request.problemId)
                .bagQueryParameters(request.submissionBag)
                .build();
        rejudgeSubmissionsUseCase.execute(submissionQueryParams);
    }

    @PostMapping("/{submissionId}/judge")
    void rejudgeSubmission(@PathVariable String submissionId) {
        rejudgeSubmissionsUseCase.execute(submissionId);
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Request {
    public int problemId;
    public Bag submissionBag;
}