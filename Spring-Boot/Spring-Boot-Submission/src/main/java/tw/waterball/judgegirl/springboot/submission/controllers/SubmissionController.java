/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.springboot.submission.controllers;

import lombok.AllArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.submission.Bag;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionservice.domain.usecases.*;
import tw.waterball.judgegirl.submissionservice.domain.usecases.dto.SubmissionQueryParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.springboot.utils.MultipartFileUtils.convertMultipartFilesToFileResources;
import static tw.waterball.judgegirl.springboot.utils.ResponseEntityUtils.respondInputStreamResource;
import static tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient.HEADER_BAG_KEY_PREFIX;
import static tw.waterball.judgegirl.submissionapi.clients.SubmissionApiClient.SUBMIT_CODE_MULTIPART_KEY_NAME;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api/problems/{problemId}/{langEnvName}/students/{studentId}/submissions")
public class SubmissionController {
    private final TokenService tokenService;
    private final SubmitCodeUseCase submitCodeUseCase;
    private final GetSubmissionUseCase getSubmissionUseCase;
    private final GetSubmissionsUseCase getSubmissionsUseCase;
    private final DownloadSubmittedCodesUseCase downloadSubmittedCodesUseCase;

    @GetMapping("/health")
    public String health(@PathVariable String langEnvName, @PathVariable int problemId, @PathVariable int studentId) {
        return String.format("OK (problemId=%d, langEnv=%s, studentId=%d)", problemId, langEnvName, studentId);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SubmissionView> submit(@RequestHeader("Authorization") String bearerToken,
                                          @PathVariable int problemId,
                                          @PathVariable String langEnvName,
                                          @PathVariable int studentId,
                                          @RequestHeader HttpHeaders headers,
                                          @RequestParam(SUBMIT_CODE_MULTIPART_KEY_NAME) MultipartFile[] submittedCodes) {
        return tokenService.returnIfTokenValid(studentId, bearerToken, (token) -> {
            boolean throttling = !token.isAdmin();
            Bag bag = getBagFromHeaders(token, headers);
            SubmitCodeRequest request = convertToSubmitCodeRequest(problemId, langEnvName, studentId, submittedCodes, bag, throttling);
            SubmissionPresenter presenter = new SubmissionPresenter();
            submitCodeUseCase.execute(request, presenter);
            return ResponseEntity.accepted().body(presenter.present());
        });
    }

    private Bag getBagFromHeaders(TokenService.Token token, HttpHeaders headers) {
        if (token.isAdmin()) {
            // 'bag' is only supported for admins
            Map<String, String> messages = new HashMap<>();
            headers.forEach((key, val) -> {
                key = key.trim();
                if (key.startsWith(HEADER_BAG_KEY_PREFIX)) {  // for example: "BAG_KEY_helloKitty"
                    String bagKey = key.substring(HEADER_BAG_KEY_PREFIX.length());  // the bagKey will be "helloKitty"
                    messages.put(bagKey, val.get(0));
                }
            });
            return new Bag(messages);
        }
        return Bag.empty();
    }

    private SubmitCodeRequest convertToSubmitCodeRequest(int problemId, String langEnvName,
                                                         int studentId, MultipartFile[] submittedCodes,
                                                         Bag bag,
                                                         boolean throttling) {
        return new SubmitCodeRequest(
                throttling, problemId, langEnvName, studentId,
                convertMultipartFilesToFileResources(submittedCodes), bag);
    }


    @GetMapping(value = "/{submissionId}")
    SubmissionView getSubmission(@RequestHeader("Authorization") String bearerToken,
                                 @PathVariable int problemId,
                                 @PathVariable String langEnvName,
                                 @PathVariable int studentId,
                                 @PathVariable String submissionId) {
        return tokenService.returnIfTokenValid(studentId, bearerToken,
                (token) -> {
                    SubmissionPresenter presenter = new SubmissionPresenter();
                    getSubmissionUseCase.execute(
                            new GetSubmissionUseCase.Request(problemId, langEnvName, studentId, submissionId), presenter);
                    return presenter.present();
                });
    }

    @GetMapping
    List<SubmissionView> getSubmissions(@RequestHeader("Authorization") String bearerToken,
                                        @RequestParam(value = "page", required = false) Integer page,
                                        @PathVariable int problemId,
                                        @PathVariable String langEnvName,
                                        @PathVariable int studentId,
                                        @RequestParam Map<String, String> bagQueryParameters) {
        bagQueryParameters.remove("page"); // only non-reserved keywords will be accepted by the bag-query filter
        return tokenService.returnIfTokenValid(studentId, bearerToken, (token) -> {
            GetSubmissionsPresenter presenter = new GetSubmissionsPresenter();
            getSubmissionsUseCase.execute(new SubmissionQueryParams(page, problemId, langEnvName, studentId, bagQueryParameters), presenter);
            return presenter.present();
        });
    }

    @GetMapping(value = "/{submissionId}/submittedCodes/{submittedCodesFileId}",
            produces = "application/zip")
    ResponseEntity<InputStreamResource> downloadZippedSubmittedCodes(@RequestHeader("Authorization") String bearerToken,
                                                                     @PathVariable int problemId,
                                                                     @PathVariable String langEnvName,
                                                                     @PathVariable int studentId,
                                                                     @PathVariable String submissionId,
                                                                     @PathVariable String submittedCodesFileId) {
        return tokenService.returnIfTokenValid(studentId, bearerToken,
                (token) -> respondInputStreamResource(
                        downloadSubmittedCodesUseCase.execute(new DownloadSubmittedCodesUseCase.Request(
                                studentId, langEnvName, submissionId, submittedCodesFileId)
                        )));
    }

}

class SubmissionPresenter implements tw.waterball.judgegirl.submissionservice.domain.usecases.SubmissionPresenter {
    private SubmissionView submissionView;

    @Override
    public void setSubmission(Submission submission) {
        this.submissionView = SubmissionView.toViewModel(submission);
    }

    public SubmissionView present() {
        return submissionView;
    }
}


class GetSubmissionsPresenter implements GetSubmissionsUseCase.Presenter {
    private List<SubmissionView> submissionViews;

    @Override
    public void setSubmissions(List<Submission> submissions) {
        this.submissionViews = submissions.stream()
                .map(SubmissionView::toViewModel).collect(Collectors.toList());
    }

    public List<SubmissionView> present() {
        return submissionViews;
    }
}