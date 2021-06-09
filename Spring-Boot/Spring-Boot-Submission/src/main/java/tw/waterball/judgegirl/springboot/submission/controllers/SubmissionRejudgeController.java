package tw.waterball.judgegirl.springboot.submission.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.submission.domain.usecases.RejudgeSubmissionsUseCase;
import tw.waterball.judgegirl.submission.domain.usecases.query.SubmissionQueryParams;

import java.util.Map;

/**
 * @author - c11037at@gmail.com (snowmancc)
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/submissions")
public class SubmissionRejudgeController {

    private final RejudgeSubmissionsUseCase rejudgeSubmissionsUseCase;

    @PostMapping(value = "/judges")
    void rejudgeAllSubmissions(@RequestBody RejudgeSubmissionsUseCase.Request request,
                               @RequestParam Map<String, String> bagQueryParameters) {
        var submissionQueryParams = SubmissionQueryParams.builder()
                .problemId(request.problemId)
                .bagQueryParameters(bagQueryParameters)
                .build();
        rejudgeSubmissionsUseCase.execute(submissionQueryParams);
    }

    @PostMapping(value = "/{submissionId}/judge")
    void rejudgeSubmission(@PathVariable String submissionId) {
        rejudgeSubmissionsUseCase.execute(submissionId);
    }
}
