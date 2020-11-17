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
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionservice.domain.usecases.SubmitCodeRequest;

import javax.inject.Named;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.createBearer;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class SubmissionApiClient extends BaseRetrofitAPI implements SubmissionServiceDriver {
    private API api;
    private String token;

    public SubmissionApiClient(RetrofitFactory retrofitFactory,
                               String scheme,
                               String host, int port,
                               String token) {
        this.token = token;
        this.api = retrofitFactory.create(scheme, host, port).create(API.class);
    }

    @Override
    public SubmissionView submit(SubmitCodeRequest submitCodeRequest) throws IOException {
        return api.submit(createBearer(token),
                submitCodeRequest.problemId, submitCodeRequest.studentId,
                submitCodeRequest.fileResources.stream()
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
    public SubmissionView getSubmission(int problemId, int studentId, String submissionId) throws NotFoundException {
        return errorHandlingGetBody(() -> api.getSubmission(
                createBearer(token),
                problemId, studentId, submissionId).execute());
    }

    @Override
    public FileResource downloadSubmittedCodes(int problemId, int studentId,
                                               String submissionId, String submittedCodesFileId) throws NotFoundException {
        Response<ResponseBody> resp = errorHandlingGetResponse(() ->
                api.getSubmittedCodes(
                        createBearer(token),
                        problemId, studentId, submissionId, submittedCodesFileId));
        return parseDownloadedFileResource(resp);
    }

    @Override
    public List<SubmissionView> getSubmissions(int problemId, int studentId) {
        return errorHandlingGetBody(() -> api.getSubmissions(
                createBearer(token),
                problemId, studentId).execute());
    }

    private interface API {
        @Multipart
        @POST("/api/problems/{problemId}/students/{studentId}/submissions")
        Call<SubmissionView> submit(@Header("Authorization") String bearerToken,
                                @Path("problemId") int problemId,
                                @Path("studentId") int studentId,
                                @Part List<MultipartBody.Part> submittedCodes);

        @GET("/api/problems/{problemId}/students/{studentId}/submissions/{submissionId}")
        Call<SubmissionView> getSubmission(@Header("Authorization") String bearerToken,
                                       @Path("problemId") int problemId,
                                       @Path("studentId") int studentId,
                                       @Path("submissionId") String submissionId);


        @GET("/api/problems/{problemId}/students/{studentId}/submissions")
        Call<List<SubmissionView>> getSubmissions(@Header("Authorization") String bearerToken,
                                              @Path("problemId") int problemId,
                                              @Path("studentId") int studentId);

        @GET("/api/problems/{problemId}/students/{studentId}/submissions/{submissionId}/submittedCodes/{submittedCodesFileId}")
        Call<ResponseBody> getSubmittedCodes(@Header("Authorization") String bearerToken,
                                             @Path("problemId") int problemId,
                                             @Path("studentId") int studentId,
                                             @Path("submissionId") String submissionId,
                                             @Path("submittedCodesFileId") String submittedCodesFileId);
    }

}
