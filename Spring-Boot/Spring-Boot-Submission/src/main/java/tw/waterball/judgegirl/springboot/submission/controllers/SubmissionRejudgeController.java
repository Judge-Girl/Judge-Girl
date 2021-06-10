package tw.waterball.judgegirl.springboot.submission.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.submission.domain.usecases.RejudgeSubmissionsUseCase;
import tw.waterball.judgegirl.submission.domain.usecases.query.SubmissionQueryParams;

import javax.servlet.http.Part;

import static tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient.SUBMISSION_BAG_MULTIPART_KEY_NAME;

/**
 * @author - c11037at@gmail.com (snowmancc)
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/submissions")
public class SubmissionRejudgeController {

    private final ObjectMapper objectMapper;
    private final RejudgeSubmissionsUseCase rejudgeSubmissionsUseCase;

    @PostMapping(value = "/judges")
    void rejudgeAllSubmissions(@RequestBody RejudgeSubmissionsUseCase.Request request,
                               @RequestParam(value = SUBMISSION_BAG_MULTIPART_KEY_NAME, required = false) Part submissionBag) {
        Bag bag = readSubmissionBag(submissionBag);
        var submissionQueryParams = SubmissionQueryParams.builder()
                .problemId(request.problemId)
                .bagQueryParameters(bag)
                .build();
        rejudgeSubmissionsUseCase.execute(submissionQueryParams);
    }

    @PostMapping(value = "/{submissionId}/judge")
    void rejudgeSubmission(@PathVariable String submissionId) {
        rejudgeSubmissionsUseCase.execute(submissionId);
    }

    @SneakyThrows
    private Bag readSubmissionBag(Part submissionBag) {
        if (submissionBag == null) {
            return Bag.empty();
        }
        return objectMapper.readValue(submissionBag.getInputStream(), Bag.class);
    }
}
