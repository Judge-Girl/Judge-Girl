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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.token.TokenInvalidException;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.commons.utils.HttpHeaderUtils;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.springboot.utils.ResponseEntityUtils;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionservice.domain.usecases.*;
import tw.waterball.judgegirl.submissionservice.domain.usecases.dto.SubmissionQueryParams;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@CrossOrigin
@RestController
@RequestMapping("/api/problems/{problemId}/{langEnvName}/students/{studentId}/submissions")
public class SubmissionController {
    public final static String SUBMIT_CODE_MULTIPART_KEY_NAME = "submittedCodes";
    private final TokenService tokenService;
    private final SubmitCodeUseCase submitCodeUseCase;
    private final GetSubmissionUseCase getSubmissionUseCase;
    private final GetSubmissionsUseCase getSubmissionsUseCase;
    private final DownloadSubmittedCodesUseCase downloadSubmittedCodesUseCase;

    public SubmissionController(TokenService tokenService,
                                SubmitCodeUseCase submitCodeUseCase,
                                GetSubmissionUseCase getSubmissionUseCase,
                                GetSubmissionsUseCase getSubmissionsUseCase,
                                DownloadSubmittedCodesUseCase downloadSubmittedCodesUseCase) {
        this.tokenService = tokenService;
        this.submitCodeUseCase = submitCodeUseCase;
        this.getSubmissionUseCase = getSubmissionUseCase;
        this.getSubmissionsUseCase = getSubmissionsUseCase;
        this.downloadSubmittedCodesUseCase = downloadSubmittedCodesUseCase;
    }

    @GetMapping("/health")
    public String health(@PathVariable String langEnvName, @PathVariable int problemId, @PathVariable int studentId) {
        return String.format("OK (problemId=%d, langEnv=%s, studentId=%d)", problemId, langEnvName, studentId);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity submit(@RequestHeader("Authorization") String bearerToken,
                          @PathVariable int problemId,
                          @PathVariable String langEnvName,
                          @PathVariable int studentId,
                          @RequestParam(SUBMIT_CODE_MULTIPART_KEY_NAME) MultipartFile[] submittedCodes) {
        return validateIdentity(studentId, bearerToken, (token) -> {
            try {
                boolean throttling = !token.isAdmin();
                SubmitCodeRequest request = convertToSubmitCodeRequest(problemId, langEnvName, studentId, submittedCodes, throttling);
                SubmissionPresenter presenter = new SubmissionPresenter();
                submitCodeUseCase.execute(request, presenter);
                return ResponseEntity.accepted().body(presenter.present());
            } catch (IOException e) {
                throw new RuntimeException("File uploading error", e);
            }
        });
    }

    private SubmitCodeRequest convertToSubmitCodeRequest(@PathVariable int problemId, String langEnvName, @PathVariable int studentId, @RequestParam(SUBMIT_CODE_MULTIPART_KEY_NAME) MultipartFile[] submittedCodes, boolean throttling) {
        return new SubmitCodeRequest(
                throttling, studentId, problemId, langEnvName,
                Arrays.stream(submittedCodes)
                        .map(this::convertMultipartFileToFileResource)
                        .collect(Collectors.toList()));
    }

    private FileResource convertMultipartFileToFileResource(MultipartFile multipartFile) {
        try {
            return new FileResource(multipartFile.getOriginalFilename(),
                    multipartFile.getSize(),
                    multipartFile.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("File uploading error", e);
        }
    }

    @GetMapping(value = "/{submissionId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity getSubmission(@RequestHeader("Authorization") String bearerToken,
                                 @PathVariable int problemId,
                                 @PathVariable String langEnvName,
                                 @PathVariable int studentId,
                                 @PathVariable String submissionId) {
        return validateIdentity(studentId, bearerToken,
                (token) -> {
                    SubmissionPresenter presenter = new SubmissionPresenter();
                    getSubmissionUseCase.execute(
                            new GetSubmissionUseCase.Request(problemId, langEnvName, studentId, submissionId),
                            presenter);
                    return ResponseEntity.ok(presenter.present());
                });
    }

    @GetMapping
    ResponseEntity getSubmissions(@RequestHeader("Authorization") String bearerToken,
                                  @RequestParam(value = "page", required = false) Integer page,
                                  @PathVariable int problemId,
                                  @PathVariable String langEnvName,
                                  @PathVariable int studentId) {
        return validateIdentity(studentId, bearerToken,
                (token) -> {
                    GetSubmissionsPresenterImpl presenter = new GetSubmissionsPresenterImpl();
                    getSubmissionsUseCase.execute(new SubmissionQueryParams(page, problemId, langEnvName, studentId), presenter);
                    return ResponseEntity.ok(presenter.present());
                });
    }

    @GetMapping(value = "/{submissionId}/submittedCodes/{submittedCodesFileId}",
            produces = "application/zip")
    ResponseEntity downloadZippedSubmittedCodes(@RequestHeader("Authorization") String bearerToken,
                                                @PathVariable int problemId,
                                                @PathVariable String langEnvName,
                                                @PathVariable int studentId,
                                                @PathVariable String submissionId,
                                                @PathVariable String submittedCodesFileId) {
        return validateIdentity(studentId, bearerToken, (token) -> {
            FileResource fileResource = downloadSubmittedCodesUseCase.execute(
                    new DownloadSubmittedCodesUseCase.Request(
                            studentId, langEnvName, submissionId, submittedCodesFileId)
            );
            return ResponseEntityUtils.respondInputStreamResource(fileResource);
        });
    }

    private <T> ResponseEntity validateIdentity(int studentId, String bearerToken, Function<TokenService.Token, ResponseEntity<T>> supplier) {
        String tokenString = HttpHeaderUtils.parseBearerToken(bearerToken);
        TokenService.Token token = tokenService.parseAndValidate(tokenString);
        try {
            if (token.canAccessStudent(studentId)) {
                return supplier.apply(token);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                        String.format("Student(id=%s) cannot access Student(id=%s)'s resource",
                                token.getClaimMap(), studentId));
            }
        } catch (TokenInvalidException err) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err.toString());
        }
    }
}

class SubmissionPresenter implements tw.waterball.judgegirl.submissionservice.domain.usecases.SubmissionPresenter {
    private SubmissionView submissionView;

    @Override
    public void setSubmission(Submission submission) {
        this.submissionView = SubmissionView.fromEntity(submission);
    }

    public SubmissionView present() {
        return submissionView;
    }
}


class GetSubmissionsPresenterImpl implements GetSubmissionsUseCase.Presenter {
    private List<SubmissionView> submissionViews;

    @Override
    public void setSubmissions(List<Submission> submissions) {
        this.submissionViews = submissions.stream()
                .map(SubmissionView::fromEntity).collect(Collectors.toList());
    }

    public List<SubmissionView> present() {
        return submissionViews;
    }
}