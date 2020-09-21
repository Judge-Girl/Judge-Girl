package tw.waterball.judgegirl.problemapi.clients;

import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import tw.waterball.judgegirl.commons.entities.TestCase;
import tw.waterball.judgegirl.commons.exceptions.ProblemNotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.profiles.productions.ServiceDriver;
import tw.waterball.judgegirl.commons.services.impl.retrofit.BaseRetrofitServiceAPI;
import tw.waterball.judgegirl.commons.services.impl.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.problemapi.controllers.views.ProblemView;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ServiceDriver
@Component
public class ProblemApiClient extends BaseRetrofitServiceAPI implements ProblemServiceDriver {
    private Api api;

    public ProblemApiClient(RetrofitFactory retrofitFactory,
                            @Value("${judge-girl.problem-service.address}") String address,
                            @Value("${judge-girl.problem-service.port}") int port) {
        this.api = retrofitFactory.create(address, port).create(Api.class);
    }

    @Override
    public ProblemView getProblem(int problemId) throws ProblemNotFoundException {
        return errorHandlingGetBody(() -> api.getProblem(problemId).execute());
    }

    @Override
    public List<TestCase> getTestCases(int problemId) throws ProblemNotFoundException {
        return errorHandlingGetBody(() -> api.getTestCases(problemId).execute());
    }

    @Override
    public FileResource downloadZippedProvidedCodes(int problemId, String providedCodesFileId) throws ProblemNotFoundException {
        Response<ResponseBody> resp = errorHandlingGetResponse(() -> api.getZippedProvidedSourceCodes(problemId
                , providedCodesFileId));
        return parseDownloadedFileResource(resp);

    }

    @Override
    public FileResource downloadZippedTestCaseIOs(int problemId, String testcaseIOsFileId) throws ProblemNotFoundException {
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
