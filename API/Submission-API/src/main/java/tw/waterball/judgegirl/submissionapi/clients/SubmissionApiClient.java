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
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.*;
import tw.waterball.judgegirl.api.retrofit.BaseRetrofitAPI;
import tw.waterball.judgegirl.api.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottlingException;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static tw.waterball.judgegirl.api.retrofit.BaseRetrofitAPI.ExceptionDeclaration.mapStatusCode;
import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.bearerWithToken;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class SubmissionApiClient extends BaseRetrofitAPI implements SubmissionServiceDriver {
    public static final String HEADER_BAG_KEY_PREFIX = "bag_key_";
    public static final String CURRENTLY_ONLY_SUPPORT_C = Language.C.toString();
    public static final String SUBMIT_CODE_MULTIPART_KEY_NAME = "submittedCodes";
    private final API api;
    private final Supplier<String> tokenSupplier;
    private final BagInterceptor[] bagInterceptors;

    public SubmissionApiClient(RetrofitFactory retrofitFactory,
                               String scheme, String host, int port,
                               Supplier<String> tokenSupplier,
                               BagInterceptor... bagInterceptors) {
        this.tokenSupplier = tokenSupplier;
        this.bagInterceptors = bagInterceptors;
        this.api = retrofitFactory.create(scheme, host, port
                /*TODO: add an interceptor that add Authorization header on every request*/)
                .create(API.class);
    }

    @Override
    public SubmissionView submit(SubmitCodeRequest request) throws SubmissionThrottlingException {
        return errorHandlingGetBody(() -> api.submit(withSubmissionBagAsHeaders(request),
                request.problemId, CURRENTLY_ONLY_SUPPORT_C, request.studentId,
                mapToList(request.fileResources, this::submittedCodesMultipartBody)).execute(),
                mapStatusCode(400).toThrow(SubmissionThrottlingException::new));
    }

    private Map<String, String> withSubmissionBagAsHeaders(SubmitCodeRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", bearerWithToken(tokenSupplier.get()));
        asList(bagInterceptors).forEach(interceptor -> interceptor.intercept(request.submissionBag));
        request.submissionBag.forEach((key, val) -> headers.put(HEADER_BAG_KEY_PREFIX + key, val));
        return headers;
    }

    @NotNull
    private MultipartBody.Part submittedCodesMultipartBody(FileResource r) {
        return MultipartBody.Part.createFormData(SUBMIT_CODE_MULTIPART_KEY_NAME, r.getFileName(),
                new RequestBody() {
                    @Override
                    public MediaType contentType() {
                        return MediaType.parse("application/zip");
                    }

                    @Override
                    public void writeTo(@NotNull BufferedSink bufferedSink) throws IOException {
                        bufferedSink.writeAll(Okio.source(r.getInputStream()));
                    }
                });
    }

    @Override
    public SubmissionView getSubmission(int problemId, int studentId, String submissionId) throws NotFoundException {
        return errorHandlingGetBody(() -> api.getSubmission(
                bearerWithToken(tokenSupplier.get()),
                problemId, CURRENTLY_ONLY_SUPPORT_C, studentId, submissionId).execute());
    }

    @Override
    public FileResource downloadSubmittedCodes(int problemId, int studentId,
                                               String submissionId, String submittedCodesFileId) throws NotFoundException {
        Response<ResponseBody> resp = errorHandlingGetResponse(() ->
                api.getSubmittedCodes(
                        bearerWithToken(tokenSupplier.get()),
                        problemId, CURRENTLY_ONLY_SUPPORT_C, studentId, submissionId, submittedCodesFileId));
        return parseDownloadedFileResource(resp);
    }

    @Override
    public List<SubmissionView> getSubmissions(int problemId, int studentId, Map<String, String> bagQueryParameters) {
        return errorHandlingGetBody(() -> api.getSubmissions(
                bearerWithToken(tokenSupplier.get()), problemId,
                CURRENTLY_ONLY_SUPPORT_C, studentId, bagQueryParameters).execute());
    }

    @Override
    public SubmissionView findBestRecord(List<String> submissionIds) {
        if (submissionIds.isEmpty()) {
            throw new IllegalArgumentException("The `submissionIds` should not be empty.");
        }
        return errorHandlingGetBody(() -> api.findBestRecord(
                bearerWithToken(tokenSupplier.get()),
                String.join(",", submissionIds)).execute());
    }

    @Override
    public SubmissionView findBestRecord(int problemId, int studentId) throws NotFoundException {
        return errorHandlingGetBody(() -> api.findBestRecord(
                problemId, CURRENTLY_ONLY_SUPPORT_C, studentId).execute());
    }

    private interface API {
        @Multipart
        @POST("/api/problems/{problemId}/{langEnvName}/students/{studentId}/submissions")
        Call<SubmissionView> submit(@HeaderMap Map<String, String> headers,
                                    @Path("problemId") int problemId,
                                    @Path("langEnvName") String langEnvName,
                                    @Path("studentId") int studentId,
                                    @Part List<MultipartBody.Part> submittedCodes);

        @GET("/api/problems/{problemId}/{langEnvName}/students/{studentId}/submissions/{submissionId}")
        Call<SubmissionView> getSubmission(@Header("Authorization") String bearerToken,
                                           @Path("problemId") int problemId,
                                           @Path("langEnvName") String langEnvName,
                                           @Path("studentId") int studentId,
                                           @Path("submissionId") String submissionId);

        @GET("/api/submissions/best")
        Call<SubmissionView> findBestRecord(@Header("Authorization") String bearerToken,
                                            @Body String submissionIdSplitByCommas);


        @GET("/api/problems/{problemId}/{langEnvName}/students/{studentId}/submissions")
        Call<List<SubmissionView>> getSubmissions(@Header("Authorization") String bearerToken,
                                                  @Path("problemId") int problemId,
                                                  @Path("langEnvName") String langEnvName,
                                                  @Path("studentId") int studentId,
                                                  @QueryMap Map<String, String> bagQueryParameters);

        @GET("/api/problems/{problemId}/{langEnvName}/students/{studentId}/submissions/{submissionId}/submittedCodes/{submittedCodesFileId}")
        Call<ResponseBody> getSubmittedCodes(@Header("Authorization") String bearerToken,
                                             @Path("problemId") int problemId,
                                             @Path("langEnvName") String langEnvName,
                                             @Path("studentId") int studentId,
                                             @Path("submissionId") String submissionId,
                                             @Path("submittedCodesFileId") String submittedCodesFileId);

        @GET("/api/problems/{problemId}/{langEnvName}/students/{studentId}/submissions/best")
        Call<SubmissionView> findBestRecord(@Path("problemId") int problemId,
                                            @Path("langEnvName") String langEnvName,
                                            @Path("studentId") int studentId);
    }

}
