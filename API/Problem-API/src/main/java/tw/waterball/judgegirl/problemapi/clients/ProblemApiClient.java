package tw.waterball.judgegirl.problemapi.clients;

import lombok.Value;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.problem.TestCase;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import javax.inject.Named;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class ProblemApiClient extends BaseRetrofitServiceAPI implements ProblemServiceDriver {
    private Api api;

    public ProblemApiClient(RetrofitFactory retrofitFactory,
                            @Value("${judge-girl.problem-service.address}") String address,
                            @Value("${judge-girl.problem-service.port}") int port) {
        this.api = retrofitFactory.create(address, port).create(Api.class);
    }

    @Override
    public ProblemView getProblem(int problemId) throws NotFoundException {
        return errorHandlingGetBody(() -> api.getProblem(problemId).execute());
    }

    @Override
    public List<TestCase> getTestCases(int problemId) throws NotFoundException {
        return errorHandlingGetBody(() -> api.getTestCases(problemId).execute());
    }

    @Override
    public FileResource downloadZippedProvidedCodes(int problemId, String providedCodesFileId) throws NotFoundException {
        Response<ResponseBody> resp = errorHandlingGetResponse(() -> api.getZippedProvidedSourceCodes(problemId
                , providedCodesFileId));
        return parseDownloadedFileResource(resp);

    }

    @Override
    public FileResource downloadZippedTestCaseIOs(int problemId, String testcaseIOsFileId) throws NotFoundException {
        Response<ResponseBody> resp = errorHandlingGetResponse(() -> api.getZippedTestCaseIOs(problemId,
                testcaseIOsFileId));
        return parseDownloadedFileResource(resp);

    }

    private interface Api {
        @GET("/api/problems/{problemId}")
        Call<ProblemView> getProblem(@Path("problemId") int problemId) throws ProblemNotFoundException;

        @GET("/api/problems/{problemId}/testcases")
        Call<List<TestCase>> getTestCases(@Path("problemId") int problemId) throws ProblemNotFoundException;

        @GET("/api/problems/{problemId}/providedCodes/{providedCodesFileId}")
        Call<ResponseBody> getZippedProvidedSourceCodes(@Path("problemId") int problemId,
                                                        @Path("providedCodesFileId") String providedCodesFileId) throws ProblemNotFoundException;

        @GET("/api/problems/{problemId}/testCaseIOs/{testcaseIOsFileId}")
        Call<ResponseBody> getZippedTestCaseIOs(@Path("problemId") int problemId,
                                                @Path("testcaseIOsFileId") String testcaseIOsFileId) throws ProblemNotFoundException;
    }
}
