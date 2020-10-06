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

package tw.waterball.judgegirl.submissionapi.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;
import tw.waterball.judgegirl.api.retrofit.BaseRetrofitAPI;
import tw.waterball.judgegirl.api.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionservice.domain.usecases.SubmissionRequest;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class SubmissionApiClient extends BaseRetrofitAPI implements SubmissionServiceDriver {
    private API api;

    public SubmissionApiClient(RetrofitFactory retrofitFactory,
                                        String address, int port) {
        this.api = retrofitFactory.create(address, port).create(API.class);
    }

    @Override
    public Submission submit(String token, SubmissionRequest submissionRequest) throws IOException {
        return api.submit("Bearer " + token,
                submissionRequest.problemId, submissionRequest.studentId,
                submissionRequest.fileResources.stream()
                        .map(r -> MultipartBody.Part.createFormData("submittedCodes", r.getFileName(),
                                new RequestBody() {
                                    @Override
                                    public MediaType contentType() {
                                        return MediaType.parse("application/zip");
                                    }

                                    @Override
                                    public void writeTo(BufferedSink bufferedSink) throws IOException {
                                        bufferedSink.writeAll(Okio.source(r.getInputStream()));
                                    }
                                })).collect(Collectors.toList())).execute().body();
    }

    @Override
    public Submission getSubmission(String token, int problemId, int studentId, String submissionId) throws NotFoundException {
        return errorHandlingGetBody(() -> api.getSubmission("Bearer " + token,
                problemId, studentId, submissionId).execute());
    }

    @Override
    public FileResource getZippedSubmittedCodes(String token, int problemId, int studentId, String submissionId) throws NotFoundException {
        Response<ResponseBody> resp = errorHandlingGetResponse(() ->
                api.getZippedSubmittedCodes("Bearer " + token,
                        problemId, studentId, submissionId));
        return parseDownloadedFileResource(resp);
    }

    @Override
    public List<Submission> getSubmissions(String token, int problemId, int studentId) {
        return errorHandlingGetBody(() -> api.getSubmissions("Bearer " + token,
                problemId, studentId).execute());
    }

    private interface API {
        @Multipart
        @POST("/api/problems/{problemId}/students/{studentId}/submissions")
        Call<Submission> submit(@Header("Authorization") String bearerToken,
                                @Path("problemId") int problemId,
                                @Path("studentId") int studentId,
                                @Part List<MultipartBody.Part> submittedCodes);

        @GET("/api/problems/{problemId}/students/{studentId}/submissions/{submissionId}")
        Call<Submission> getSubmission(@Header("Authorization") String bearerToken,
                                       @Path("problemId") int problemId,
                                       @Path("studentId") int studentId,
                                       @Path("submissionId") String submissionId);


        @GET("/api/problems/{problemId}/students/{studentId}/submissions")
        Call<List<Submission>> getSubmissions(@Header("Authorization") String bearerToken,
                                              @Path("problemId") int problemId,
                                              @Path("studentId") int studentId);

        @GET("/api/problems/{problemId}/students/{studentId}/submissions/{submissionId}/zippedSubmittedCodes")
        Call<ResponseBody> getZippedSubmittedCodes(@Header("Authorization") String bearerToken,
                                                   @Path("problemId") int problemId,
                                                   @Path("studentId") int studentId,
                                                   @Path("submissionId") String submissionId);
    }

    public static void main(String[] args) throws IOException {
        int studentId = 1;
        int problemId = 1;
        String token = studentId + "," + Long.MAX_VALUE;

        SubmissionServiceDriver submissionService = new SubmissionApiClient(
                new RetrofitFactory(new ObjectMapper()), "127.0.0.1", 33003);
        byte[] a = "a".getBytes();
        byte[] b = "b".getBytes();
        Submission submission = submissionService.submit(token, new SubmissionRequest(1, 1,
                Arrays.asList(new FileResource("a", a.length, new ByteArrayInputStream(a)),
                        new FileResource("b", b.length, new ByteArrayInputStream(b)))));
        System.out.println(submission);

        FileResource codes = submissionService.getZippedSubmittedCodes(token, studentId, problemId, submission.getId());
        System.out.println(submissionService.getSubmissions(token, studentId, problemId));

        System.out.println(submissionService.getSubmission(token, studentId, problemId, submission.getId()));

    }

}
