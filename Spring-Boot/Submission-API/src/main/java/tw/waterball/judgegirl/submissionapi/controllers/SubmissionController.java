/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.submissionapi.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.models.files.SubmissionRequest;
import tw.waterball.judgegirl.commons.services.token.TokenInvalidException;
import tw.waterball.judgegirl.commons.services.token.TokenService;
import tw.waterball.judgegirl.commons.utils.HttpHeaderUtils;
import tw.waterball.judgegirl.commons.utils.ResponseEntityUtils;
import tw.waterball.judgegirl.submissionservice.domain.entities.Submission;
import tw.waterball.judgegirl.submissionservice.impl.services.SubmissionService;
import tw.waterball.judgegirl.submissionservice.impl.services.dto.SubmissionQueryParams;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@CrossOrigin
@RestController
@RequestMapping("/api/problems/{problemId}/students/{studentId}/submissions")
public class SubmissionController {
    public final static String MULTIPART_KEY_NAME = "submittedCodes";
    private TokenService tokenService;
    private SubmissionService submissionService;
    private SubmissionThrottlingService submissionThrottlingService;

    public SubmissionController(TokenService tokenService,
                                SubmissionService submissionService,
                                SubmissionThrottlingService submissionThrottlingService) {
        this.tokenService = tokenService;
        this.submissionService = submissionService;
        this.submissionThrottlingService = submissionThrottlingService;
    }

    @PostMapping
    ResponseEntity submit(@RequestHeader("Authorization") String bearerToken,
                          @PathVariable int problemId, @PathVariable int studentId,
                          @RequestParam(MULTIPART_KEY_NAME) MultipartFile[] submittedCodes) {
        return validateIdentity(studentId, bearerToken, (token) -> {
            try {
                SubmissionRequest request = new SubmissionRequest(studentId, problemId,
                        Arrays.stream(submittedCodes)
                                .map(this::convertMultipartFileToFileResource)
                                .collect(Collectors.toList()));
                if (!token.isAdmin() && studentId != 1434 /*TODO hard-coded debug purpose admin id*/) {
                    submissionThrottlingService.validateRequest(request);
                }
                Submission submission = submissionService.submit(request);
                return ResponseEntity.accepted().body(submission);
            } catch (IOException e) {
                throw new RuntimeException("File uploading error", e);
            }
        });
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

    @GetMapping("/{submissionId}")
    ResponseEntity getSubmission(@RequestHeader("Authorization") String bearerToken,
                                 @PathVariable int problemId, @PathVariable int studentId,
                                 @PathVariable String submissionId) {
        return validateIdentity(studentId, bearerToken,
                (token) -> ResponseEntity.ok(
                        submissionService.getSubmission(problemId, studentId, submissionId)));
    }

    @GetMapping
    ResponseEntity getSubmissions(@RequestHeader("Authorization") String bearerToken,
                                  @RequestParam(value = "page", required = false) Integer page,
                                  @PathVariable int problemId, @PathVariable int studentId) {
        return validateIdentity(studentId, bearerToken,
                (token) -> ResponseEntity.ok(
                        submissionService.getSubmissions(new SubmissionQueryParams(page, problemId, studentId))));

    }

    @GetMapping(value = "/{submissionId}/zippedSubmittedCodes",
            produces = "application/zip")
    ResponseEntity getZippedSubmittedCodes(@RequestHeader("Authorization") String bearerToken,
                                           @PathVariable int problemId, @PathVariable int studentId,
                                           @PathVariable String submissionId) {
        return validateIdentity(studentId, bearerToken, (token) -> {
            FileResource fileResource = submissionService.getZippedSubmittedCodes(submissionId);
            return ResponseEntityUtils.respondInputStreamResource(fileResource);
        });
    }


    // TODO should integrate Spring Security in the future to reduce duplicate auth codes by AOP
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
