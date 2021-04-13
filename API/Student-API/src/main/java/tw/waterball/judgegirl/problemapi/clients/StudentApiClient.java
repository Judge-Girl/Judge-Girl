package tw.waterball.judgegirl.problemapi.clients;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import tw.waterball.judgegirl.api.retrofit.BaseRetrofitAPI;
import tw.waterball.judgegirl.api.retrofit.RetrofitFactory;
import tw.waterball.judgegirl.entities.Student;

import java.util.List;

public class StudentApiClient extends BaseRetrofitAPI implements StudentServiceDriver {

    private Api api;
    private String adminToken;

    public StudentApiClient(RetrofitFactory retrofitFactory,
                            String scheme,
                            String host, int port,
                            String adminToken) {
        this.adminToken = adminToken;
        this.api = retrofitFactory.create(scheme, host, port).create(Api.class);
    }

    @Override
    public List<Student> getStudentsByEmails(List<String> emails) {
        return errorHandlingGetBody(() -> api.getStudentsByEmails(emails).execute());
    }

    private interface Api {
        @POST("/api/students/search")
        Call<List<Student>> getStudentsByEmails(@Body List<String> emails);
    }
}
