package tw.waterball.judgegirl.studentapi.clients;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import tw.waterball.judgegirl.api.retrofit.BaseRetrofitAPI;
import tw.waterball.judgegirl.api.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.primitives.Student;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class StudentApiClient extends BaseRetrofitAPI implements StudentServiceDriver {
    private final Api api;
    private final Supplier<String> tokenSupplier;

    public StudentApiClient(RetrofitFactory retrofitFactory,
                            String scheme,
                            String host, int port,
                            Supplier<String> tokenSupplier) {
        this.tokenSupplier = tokenSupplier;
        this.api = retrofitFactory.create(scheme, host, port).create(Api.class);
    }

    @Override
    public List<Student> getStudentsByIds(List<Integer> ids) {
        String idsSplitByComma = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        return errorHandlingGetBody(() -> api.getStudentsByIds(idsSplitByComma).execute());
    }

    @Override
    public List<Student> getStudentsByEmails(List<String> emails) {
        return errorHandlingGetBody(() -> api.getStudentsByEmails(emails).execute());
    }

    private interface Api {
        @GET("/api/students")
        Call<List<Student>> getStudentsByIds(@Query("ids") String idsSplitByComma);

        @POST("/api/students/search")
        Call<List<Student>> getStudentsByEmails(@Body List<String> emails);
    }
}
