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

package tw.waterball.judgegirl.springboot.student.controllers.it;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.SpringBootStudentApplication;
import tw.waterball.judgegirl.springboot.student.controllers.LoginResponse;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.JpaStudentDataPort;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignInUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.springboot.student.view.StudentView.toViewModel;


/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootStudentApplication.class)
public class StudentControllerIT extends AbstractSpringBootTest {
    private Student student;

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

    @Test
    void WhenSignUpCorrectly_ShouldRespondStudentView() throws Exception {
        StudentView body = signUpAndGetResponseBody(student);
        student.setId(body.getId());
        assertEquals(toViewModel(student), body);
    }

    @Test
    void WhenSignUpWithEmptyName_ShouldRespondBadRequest() throws Exception {
        signUp("", "email@example.com", "password")
                .andExpect(status().isBadRequest());
    }

    @Test
    void WhenSignUpWithEmptyPassword_ShouldRespondBadRequest() throws Exception {
        signUp("name", "email@example.com", "")
                .andExpect(status().isBadRequest());
    }

    @Test
    void WhenSignUpWithIncorrectEmail_ShouldRespondBadRequest() throws Exception {
        signUp("name", "email", "password")
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenOneStudentSignedUp_WhenSignUpWithExistingEmail_ShouldRespondBadRequest() throws Exception {
        signUp(student);
        student = new Student("name", "email@example.com", "password");
        mockMvc.perform(post("/api/students/signUp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(student)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenOneStudentSignedUp_WhenLoginCorrectly_ShouldRespondLoginResponseWithCorrectToken() throws Exception {
        StudentView studentView = signUpAndGetResponseBody(student);
        LoginResponse body = signInAndGetResponseBody(this.student.getEmail(), this.student.getPassword());

        assertEquals(studentView.getId(), body.id);
        assertEquals(studentView.getEmail(), body.email);
        TokenService.Token token = tokenService.parseAndValidate(body.token);
        assertEquals(studentView.getId(), token.getStudentId());
    }

    private StudentView signUpAndGetResponseBody(Student student) throws Exception {
        return getBody(signUp(student).andExpect(status().isOk()), StudentView.class);
    }

    private ResultActions signUp(Student student) throws Exception {
        return mockMvc.perform(post("/api/students/signUp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(student)));
    }

    private ResultActions signUp(String name, String email, String password) throws Exception {
        Student newStudent = new Student(name, email, password);
        return mockMvc.perform(post("/api/students/signUp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(newStudent)));
    }

    @Test
    void GivenOneStudentSignedUp_WhenLoginWithWrongPassword_ShouldRespondBadRequest() throws Exception {
        signUp(student);

        signIn(this.student.getEmail(), "wrongPassword")
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenOneStudentSignedUp_WhenLoginWithWrongEmail_ShouldRespondNotFound() throws Exception {
        signUp(student);

        signIn("worngEmail@example.com", this.student.getPassword())
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenOneStudentSignedUp_WhenLoginWithWrongEmailAndPassword_ShouldRespondNotFound() throws Exception {
        signUp(student);

        signIn("worngEmail@example.com", "wrongPassword")
                .andExpect(status().isNotFound());
    }

    private LoginResponse signInAndGetResponseBody(String email, String password) throws Exception {
        return getBody(signIn(email, password).andExpect(status().isOk()), LoginResponse.class);
    }

    private ResultActions signIn(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new SignInUseCase.Request(email, password))));
    }

    @Test
    void GivenOneStudentSignedUp_WhenGetStudentById_ShouldRespondStudentView() throws Exception {
        StudentView student = signUpAndGetResponseBody(this.student);
        LoginResponse loginResponse = signInAndGetResponseBody(this.student.getEmail(), this.student.getPassword());

        StudentView body = getBody(getStudentById(student.getId(), loginResponse.token)
                .andExpect(status().isOk()), StudentView.class);

        this.student.setId(body.getId());
        assertEquals(toViewModel(this.student), body);
    }

    @Test
    void WhenGetStudentByNonExistingStudentId_ShouldRespondNotFound() throws Exception {
        int nonExistingStudentId = 123123;
        TokenService.Token token = tokenService.createToken(new TokenService.Identity(nonExistingStudentId));

        getStudentById(nonExistingStudentId, token.getToken())
                .andExpect(status().isNotFound());
    }

    private ResultActions getStudentById(Integer id, String tokenString) throws Exception {
        return mockMvc.perform(get("/api/students/" + id)
                .header("Authorization", "bearer " + tokenString));
    }

    @Test
    void GivenOneStudentSignedUp_WhenAuth_ShouldRespondLoginResponseWithNewToken() throws Exception {
        signUp(student);
        LoginResponse loginResponse = signInAndGetResponseBody(student.getEmail(), student.getPassword());

        LoginResponse authResponse = authAndGetResponseBody(loginResponse.token);

        assertEquals(loginResponse.token, authResponse.token);
        assertNotEquals(loginResponse.expiryTime, authResponse.expiryTime);
        assertEquals(loginResponse.id, authResponse.id);
        assertEquals(loginResponse.email, authResponse.email);
    }

    @Test
    void WhenAuthWithNonExistingStudentToken_ShouldRespondUnauthorized() throws Exception {
        int nonExistingStudentId = 123123;
        TokenService.Token token = tokenService.createToken(new TokenService.Identity(nonExistingStudentId));
        auth(token.getToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void WhenAuthWithInvalidToken_ShouldRespondUnauthorized() throws Exception {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdHVkZW50SWQiOjEsImV4cCI6MTYxNTgzMDMwOH0.bI1j9-fCT0Ubd8ntuFstTo-UAXxopvGZLOFYwyAmnX8";
        auth(invalidToken).andExpect(status().isUnauthorized());
    }

    private LoginResponse authAndGetResponseBody(String tokenString) throws Exception {
        return getBody(mockMvc.perform(post("/api/students/auth")
                .header("Authorization", "bearer " + tokenString))
                .andExpect(status().isOk()), LoginResponse.class);
    }

    private ResultActions auth(String tokenString) throws Exception {
        return mockMvc.perform(post("/api/students/auth")
                .header("Authorization", "bearer " + tokenString));
    }

}
