package tw.waterball.judgegirl.springboot.student.controllers.it;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.SpringBootProblemApplication;
import tw.waterball.judgegirl.springboot.student.controllers.LoginResponse;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.JpaStudentDataPort;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignInUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.springboot.student.view.StudentView.toViewModel;


/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@ContextConfiguration(classes = SpringBootProblemApplication.class)
public class StudentControllerIT extends AbstractSpringBootTest {
    private Student student;
    private SignInUseCase.Request request;

    @Autowired
    private JpaStudentDataPort studentRepository;

    @Autowired
    private TokenService tokenService;

    @BeforeEach
    void setup() {
        student = new Student("name", "email@example.com", "password");
    }

    @AfterEach
    void cleanUp() {
        studentRepository.deleteAll();
    }


    StudentView signUp(Student student) throws Exception {
        return getBody(mockMvc.perform(post("/api/students/signUp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(student))), StudentView.class);
    }

    void signIn(String email, String password) {
        request = new SignInUseCase.Request(email, password);
    }

    @Test
    void WhenSignUpCorrectly_ShouldReturnStudent() throws Exception {
        StudentView body = getBody(mockMvc.perform(post("/api/students/signUp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(student)))
                .andExpect(status().isOk()), StudentView.class);

        student.setId(body.getId());
        assertEquals(toViewModel(student), body);
    }

    @Test
    void GivenSignUpAccount_WhenLoginCorrectly_ShouldReturnLoginResponse() throws Exception {
        StudentView studentView = signUp(student);
        signIn("email@example.com", "password");
        LoginResponse body = getBody(mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isOk()), LoginResponse.class);

        assertEquals(body.id, studentView.getId());
        assertEquals(body.email, request.getEmail());
        String[] parts = body.token.split(":");
        assertEquals(parts[0], studentView.getId().toString());
        //TODO: Can i get the token and token expiration time ?
    }

    @Test
    void GivenSignUpAccount_WhenLoginWithWrongPassword_ShouldReturn400() throws Exception {
        StudentView studentView = signUp(student);
        signIn("email@example.com", "wrongPassword");
        mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenSignUpAccount_WhenLoginWithWrongEmail_ShouldReturn404() throws Exception {
        StudentView studentView = signUp(student);
        signIn("worngEmail@example.com", "password");
        mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenSignUpAccount_WhenLoginWithWrongEmailAndPassword_ShouldReturn404() throws Exception {
        StudentView studentView = signUp(student);
        signIn("worngEmail@example.com", "wrongPassword");
        mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenExistedStudentId_WhenGetStudent_ShouldReturnStudent() throws Exception {
        StudentView studentView = signUp(student);
        StudentView body = getBody(mockMvc.perform(get("/api/students/" + studentView.getId()))
                .andExpect(status().isOk()), StudentView.class);

        student.setId(body.getId());
        assertEquals(toViewModel(student), body);
    }

    @Test
    void GivenNotExistedStudentId_WhenGetStudent_ShouldReturn404() throws Exception {
        StudentView studentView = signUp(student);
        mockMvc.perform(get("/api/students/2"))
                .andExpect(status().isNotFound());
    }

}
