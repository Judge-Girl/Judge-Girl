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
import tw.waterball.judgegirl.entities.Admin;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.SpringBootStudentApplication;
import tw.waterball.judgegirl.springboot.student.controllers.LoginResponse;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignInUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.springboot.student.view.StudentView.toViewModel;

@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootStudentApplication.class)
class AdminControllerTest extends AbstractSpringBootTest {

    private Student admin;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TokenService tokenService;

    @BeforeEach
    void setup() {
        admin = new Admin("adminName", "admin@example.com", "adminPassword");
    }

    @AfterEach
    void cleanUp() {
        studentRepository.deleteAll();
    }

    @Test
    void WhenAdminSignUpCorrectly_ShouldRespondStudentView() throws Exception {
        StudentView body = signUpAdminAndGetResponseBody(admin);
        admin.setId(body.id);
        assertEquals(toViewModel(admin), body);
    }

    @Test
    void WhenSignUpWithEmptyName_ShouldRespondBadRequest() throws Exception {
        signUpAdmin("", "email@example.com", "password")
                .andExpect(status().isBadRequest());
    }

    @Test
    void WhenSignUpWithEmptyPassword_ShouldRespondBadRequest() throws Exception {
        signUpAdmin("name", "email@example.com", "")
                .andExpect(status().isBadRequest());
    }

    @Test
    void WhenSignUpWithIncorrectEmail_ShouldRespondBadRequest() throws Exception {
        signUpAdmin("name", "email", "password")
                .andExpect(status().isBadRequest());
    }

    @Test
    void WhenSignUpWithPasswordOfLength3_ShouldRespondBadRequest() throws Exception {
        signUpAdmin("name", "email@example.com", "pwd")
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenOneAdminSignedUp_WhenAdminLoginCorrectly_ShouldRespondLoginResponseWithCorrectToken() throws Exception {
        StudentView studentView = signUpAdminAndGetResponseBody(admin);
        LoginResponse body = signInAdminAndGetResponseBody(this.admin.getEmail(), this.admin.getPassword());

        verifyStudentLogin(studentView, body);
    }

    private StudentView signUpAdminAndGetResponseBody(Student admin) throws Exception {
        return getBody(signUpAdmin(admin).andExpect(status().isOk()), StudentView.class);
    }

    private ResultActions signUpAdmin(String name, String email, String password) throws Exception {
        Student newAdmin = new Admin(name, email, password);
        return signUpAdmin(newAdmin);
    }

    private ResultActions signUpAdmin(Student admin) throws Exception {
        return mockMvc.perform(post("/api/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(admin)));
    }

    private LoginResponse signInAdminAndGetResponseBody(String email, String password) throws Exception {
        return getBody(signInAdmin(email, password).andExpect(status().isOk()), LoginResponse.class);
    }

    private ResultActions signInAdmin(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/admins/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new SignInUseCase.Request(email, password))));
    }

    private void verifyStudentLogin(StudentView view, LoginResponse body) {
        assertEquals(view.id, body.id);
        assertEquals(view.email, body.email);
        TokenService.Token token = tokenService.parseAndValidate(body.token);
        assertEquals(view.id, token.getStudentId());
        assertEquals(view.admin, body.admin);
    }

}