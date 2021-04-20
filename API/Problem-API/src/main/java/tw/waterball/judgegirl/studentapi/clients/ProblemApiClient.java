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

package tw.waterball.judgegirl.studentapi.clients;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import tw.waterball.judgegirl.api.retrofit.BaseRetrofitAPI;
import tw.waterball.judgegirl.api.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.bearerWithToken;

/**
 * @author - johnny850807@gmail.com (Wateqrball)
 */
public class ProblemApiClient extends BaseRetrofitAPI implements ProblemServiceDriver {
    private final Api api;
    private final String adminToken;

    public ProblemApiClient(RetrofitFactory retrofitFactory,
                            String scheme,
                            String host, int port,
                            String adminToken) {
        this.adminToken = adminToken;
        this.api = retrofitFactory.create(scheme, host, port).create(Api.class);
    }

    @Override
    public ProblemView getProblem(int problemId) throws NotFoundException {
        return errorHandlingGetBody(() -> api.getProblem(problemId).execute());
    }


    @Override
    public FileResource downloadProvidedCodes(int problemId, String languageEnvName, String providedCodesFileId) throws NotFoundException {
        Response<ResponseBody> resp = errorHandlingGetResponse(() -> api.getZippedProvidedCodes(
                bearerWithToken(adminToken),
                problemId, languageEnvName, providedCodesFileId));
        return parseDownloadedFileResource(resp);

    }

    @Override
    public FileResource downloadTestCaseIOs(int problemId, String testcaseIOsFileId) throws NotFoundException {
        System.out.printf("Problem Id: %d, Testcase IOs file id: %s.\n", problemId, testcaseIOsFileId);
        Response<ResponseBody> resp = errorHandlingGetResponse(() ->
                api.getZippedTestCaseIOs(
                        bearerWithToken(adminToken), problemId, testcaseIOsFileId));
        return parseDownloadedFileResource(resp);
    }

    private interface Api {
        @GET("/api/problems/{problemId}")
        Call<ProblemView> getProblem(@Path("problemId") int problemId) throws NotFoundException;

        @GET("/api/problems/{problemId}/{langEnvName}/providedCodes/{providedCodesFileId}")
        Call<ResponseBody> getZippedProvidedCodes(@Header("Authorization") String authorizationHeader,
                                                  @Path("problemId") int problemId,
                                                  @Path("langEnvName") String langEnvName,
                                                  @Path("providedCodesFileId") String providedCodesFileId) throws NotFoundException;

        @GET("/api/problems/{problemId}/testcaseIOs/{testcaseIOsFileId}")
        Call<ResponseBody> getZippedTestCaseIOs(@Header("Authorization") String authorizationHeader,
                                                @Path("problemId") int problemId,
                                                @Path("testcaseIOsFileId") String testcaseIOsFileId) throws NotFoundException;
    }
}
