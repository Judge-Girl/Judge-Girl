package tw.waterball.judgegirl.problemapi.clients;

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
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import javax.inject.Named;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.createBearer;

/**
 * @author - johnny850807@gmail.com (Wateqrball)
 */
@Named
public class ProblemApiClient extends BaseRetrofitAPI implements ProblemServiceDriver {
    private Api api;
    private String adminToken;

    public ProblemApiClient(RetrofitFactory retrofitFactory,
                            String baseUrl, int port,
                            String adminToken) {
        this.adminToken = adminToken;
        this.api = retrofitFactory.create(baseUrl, port).create(Api.class);
    }

    @Override
    public ProblemView getProblem(int problemId) throws NotFoundException {
        return errorHandlingGetBody(() -> api.getProblem(problemId).execute());
    }

    @Override
    public List<Testcase> getTestcases(int problemId) throws NotFoundException {
        return errorHandlingGetBody(() -> api.getTestCases(problemId).execute());
    }

    @Override
    public FileResource downloadProvidedCodes(int problemId, String providedCodesFileId) throws NotFoundException {
        Response<ResponseBody> resp = errorHandlingGetResponse(() -> api.getZippedProvidedSourceCodes(
                createBearer(adminToken),
                problemId, providedCodesFileId));
        return parseDownloadedFileResource(resp);

    }

    @Override
    public FileResource downloadTestCaseIOs(int problemId, String testcaseIOsFileId) throws NotFoundException {
        Response<ResponseBody> resp = errorHandlingGetResponse(() -> api.getZippedTestCaseIOs(
                createBearer(adminToken),
                problemId, testcaseIOsFileId));
        return parseDownloadedFileResource(resp);
    }

    private interface Api {
        @GET("/api/problems/{problemId}")
        Call<ProblemView> getProblem(@Path("problemId") int problemId) throws NotFoundException;

        @GET("/api/problems/{problemId}/testcases")
        Call<List<Testcase>> getTestCases(@Path("problemId") int problemId) throws NotFoundException;

        @GET("/api/problems/{problemId}/providedCodes/{providedCodesFileId}")
        Call<ResponseBody> getZippedProvidedSourceCodes(@Header("Authorization") String authorizationHeader,
                                                        @Path("problemId") int problemId,
                                                        @Path("providedCodesFileId") String providedCodesFileId) throws NotFoundException;

        @GET("/api/problems/{problemId}/testCaseIOs/{testcaseIOsFileId}")
        Call<ResponseBody> getZippedTestCaseIOs(@Header("Authorization") String authorizationHeader,
                                                @Path("problemId") int problemId,
                                                @Path("testcaseIOsFileId") String testcaseIOsFileId) throws NotFoundException;
    }
}
