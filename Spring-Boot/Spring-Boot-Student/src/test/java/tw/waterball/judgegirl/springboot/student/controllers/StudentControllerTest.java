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

package tw.waterball.judgegirl.springboot.student.controllers;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.SneakyThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.primitives.Admin;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.SpringBootStudentApplication;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.ChangePasswordUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.LoginUseCase;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.range;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.student;
import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.bearerWithToken;
import static tw.waterball.judgegirl.studentapi.clients.view.StudentView.toViewModel;


/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@ActiveProfiles(Profiles.JWT)
@ContextConfiguration(classes = SpringBootStudentApplication.class)
public class StudentControllerTest extends AbstractSpringBootTest {
    private Student student;

    @Autowired
    private StudentRepository studentRepository;

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
    void WhenStudentSignUpCorrectly_ShouldRespondStudentView() throws Exception {
        var body = signUpAndGetStudent(student);
        student.setId(body.id);
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
    void WhenSignUpWithPasswordOfLength3_ShouldRespondBadRequest() throws Exception {
        signUp("name", "email@example.com", "pwd")
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenOneStudentSignedUp_WhenSignUpWithDuplicateEmail_ShouldRespondBadRequest() throws Exception {
        signUp(student);

        signUp("student", student.getEmail(), "password")
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenOneStudentSignedUp_WhenStudentLoginCorrectly_ShouldRespondLoginResponseWithCorrectToken() throws Exception {
        var studentView = signUpAndGetStudent(student);
        LoginResponse body = loginAndGetResponseBody(this.student.getEmail(), this.student.getPassword());

        verifyStudentLogin(studentView, body);
    }


    @Test
    void GivenOneStudentSignedUp_WhenLoginWithWrongPassword_ShouldRespondBadRequest() throws Exception {
        signUp(student);

        login(this.student.getEmail(), "wrongPassword")
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenOneStudentSignedUp_WhenLoginWithWrongEmail_ShouldRespondNotFound() throws Exception {
        signUp(student);

        login("worngEmail@example.com", this.student.getPassword())
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenOneStudentSignedUp_WhenLoginWithWrongEmailAndPassword_ShouldRespondNotFound() throws Exception {
        signUp(student);

        login("worngEmail@example.com", "wrongPassword")
                .andExpect(status().isNotFound());
    }


    @Test
    void GivenOneStudentSignedUp_WhenGetStudentById_ShouldRespondStudentView() throws Exception {
        var student = signUpAndGetStudent(this.student);
        LoginResponse loginResponse = loginAndGetResponseBody(this.student.getEmail(), this.student.getPassword());

        StudentView body = getBody(getStudentById(student.id, loginResponse.token)
                .andExpect(status().isOk()), StudentView.class);

        this.student.setId(body.id);
        assertEquals(toViewModel(this.student), body);
    }

    @Test
    void WhenGetStudentByNonExistingStudentId_ShouldRespondNotFound() throws Exception {
        int nonExistingStudentId = 123123;
        TokenService.Token token = tokenService.createToken(student(nonExistingStudentId));

        getStudentById(nonExistingStudentId, token.getToken())
                .andExpect(status().isNotFound());
    }

    @Test
    void GivenThreeStudents_A_B_C_SignedUp_WhenGetStudents_B_C_D_ByIds_ShouldRespond_B_C() throws Exception {
        Student A = signUpAndGet(new Student("nameA", "a@example.com", "12345678"));
        Student B = signUpAndGet(new Student("nameB", "b@example.com", "12345678"));
        Student C = signUpAndGet(new Student("nameC", "c@example.com", "12345678"));
        final int STUDENT_D_ID = 9999; // non-existing

        List<Student> students = getStudentByIds(B.getId(), C.getId(), STUDENT_D_ID);

        studentsShouldHaveEmails(students, "b@example.com", "c@example.com");
    }

    private List<Student> getStudentByIds(Integer... ids) throws Exception {
        String idsSplitByComma = stream(ids).map(String::valueOf).collect(joining(","));
        return getBody(mockMvc.perform(get("/api/students").queryParam("ids", idsSplitByComma))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    @Test
    void GivenThreeStudents_A_B_C_SignedUp_WhenGetStudents_B_C_D_ByEmails_ShouldRespond_B_C() throws Exception {
        Student A = new Student("nameA", "a@example.com", "12345678");
        Student B = new Student("nameB", "b@example.com", "12345678");
        Student C = new Student("nameC", "c@example.com", "12345678");
        signUpStudents(A, B, C);

        String[] emails = {"b@example.com", "c@example.com", "d@example.com"};
        List<Student> students = getStudentsByEmail(emails);

        studentsShouldHaveEmails(students, "b@example.com", "c@example.com");
    }

    private void studentsShouldHaveEmails(List<Student> students, String... emails) {
        assertEquals(emails.length, students.size());
        for (int i = 0; i < students.size(); i++) {
            assertEquals(emails[i], students.get(i).getEmail());
        }
    }

    @Test
    void GivenOneStudentSignedUp_WhenAuth_ShouldRespondLoginResponseWithNewToken() throws Exception {
        signUp(student);
        LoginResponse loginResponse = loginAndGetResponseBody(student.getEmail(), student.getPassword());

        // we must delay certain seconds so that our token's expiry date
        // will increase enough to make differences in its produced token
        Thread.sleep(2000);

        LoginResponse authResponse = authAndGetResponseBody(loginResponse.token);

        assertNotEquals(loginResponse.token, authResponse.token, "The renewed token must be different from the original one.");
        assertNotEquals(loginResponse.expiryTime, authResponse.expiryTime);
        assertEquals(loginResponse.id, authResponse.id);
        assertEquals(loginResponse.email, authResponse.email);
    }

    @Test
    void WhenAuthWithNonExistingStudentToken_ShouldRespondUnauthorized() throws Exception {
        int nonExistingStudentId = 123123;
        TokenService.Token token = tokenService.createToken(student(nonExistingStudentId));
        auth(token.getToken()).andExpect(status().isUnauthorized());
    }

    @Test
    void WhenAuthWithInvalidToken_ShouldRespondUnauthorized() throws Exception {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdHVkZW50SWQiOjEsImV4cCI6MTYxNTgzMDMwOH0.bI1j9-fCT0Ubd8ntuFstTo-UAXxopvGZLOFYwyAmnX8";
        auth(invalidToken).andExpect(status().isUnauthorized());
    }

    @Test
    void GivenOneStudentSignedUp_WhenChangePasswordWithCorrectCurrentPassword_ShouldSucceed() throws Exception {
        signUp(student);
        LoginResponse response = loginAndGetResponseBody(student.getEmail(), student.getPassword());

        String newPassword = "newPassword";
        changePassword(student.getPassword(), newPassword, response.id, response.token).andExpect(status().isOk());

        Student student = studentRepository.findStudentById(response.id).orElseThrow();
        assertNotEquals(newPassword, student.getPassword());
    }

    @Test
    void GivenOneStudentSignedUp_WhenChangePasswordWithWrongCurrentPassword_ShouldRejectWithBadRequest() throws Exception {
        signUp(student);
        LoginResponse body = loginAndGetResponseBody(student.getEmail(), student.getPassword());

        String wrongPassword = "wrongPassword";
        String newPassword = "newPassword";
        changePassword(wrongPassword, newPassword, body.id, body.token).andExpect(status().isBadRequest());

        Student student = studentRepository.findStudentById(body.id).orElseThrow();
        assertEquals(student.getPassword(), student.getPassword());
    }

    @Test
    void Given4AdminsAnd10Students_0_to_10_WhenGetStudentsWithSkip3Size4_ShouldRespondStudents_3_to_6() throws Exception {
        signUp10StudentsAnd4Admins();

        List<StudentView> students = getBody(
                mockMvc.perform(get("/api/students?skip=3&&size=4"))
                        .andExpect(status().isOk()), new TypeReference<>() {
                });

        assertEquals(4, students.size());
        assertEquals("student3", students.get(0).name);
        assertEquals("student4", students.get(1).name);
        assertEquals("student5", students.get(2).name);
        assertEquals("student6", students.get(3).name);
    }

    @Test
    void Given10StudentsAnd4Admins0_1_2_3_WhenGetAdminsWithSkip2Size2_ShouldRespondAdmins_2_3() throws Exception {
        signUp10StudentsAnd4Admins();

        List<StudentView> students = getBody(
                mockMvc.perform(get("/api/admins?skip=2&&size=2"))
                        .andExpect(status().isOk()), new TypeReference<>() {
                });

        assertEquals(2, students.size());
        assertEquals("admin2", students.get(0).name);
        assertEquals("admin3", students.get(1).name);
    }

    @Test
    void GivenStudentSignedUp_WhenDeleteTheStudent_ShouldSucceed() throws Exception {
        var student = signUpAndGetStudent(this.student);

        deleteStudent(student.id).andExpect(status().isOk());

        assertTrue(studentRepository.findStudentById(student.id).isEmpty());
    }

    @Test
    void WhenDeleteNonExistingStudent_ShouldSucceed() throws Exception {
        int nonExistingStudentId = 123;
        deleteStudent(nonExistingStudentId).andExpect(status().isOk());
    }

    private StudentView signUpAndGetStudent(Student student) throws Exception {
        return getBody(signUp(student).andExpect(status().isOk()), StudentView.class);
    }

    private void signUpStudents(Student... students) throws Exception {
        for (Student value : students) {
            signUp(value);
        }
    }

    @SneakyThrows
    private ResultActions signUp(String name, String email, String password) {
        Student newStudent = new Student(name, email, password);
        return signUp(newStudent);
    }

    private Student signUpAndGet(Student student) throws Exception {
        return getBody(mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(student))), Student.class);
    }

    private ResultActions signUp(Student student) throws Exception {
        return mockMvc.perform(post("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(student)));
    }

    @SneakyThrows
    private ResultActions signUpAdmin(String name, String email, String password) {
        Student newAdmin = new Admin(name, email, password);
        return signUpAdmin(newAdmin);
    }

    private ResultActions signUpAdmin(Student admin) throws Exception {
        return mockMvc.perform(post("/api/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(admin)));
    }

    private LoginResponse loginAndGetResponseBody(String email, String password) throws Exception {
        return getBody(login(email, password).andExpect(status().isOk()), LoginResponse.class);
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(new LoginUseCase.Request(email, password))));
    }

    private void verifyStudentLogin(StudentView view, LoginResponse body) {
        assertEquals(view.id, body.id);
        assertEquals(view.email, body.email);
        TokenService.Token token = tokenService.parseAndValidate(body.token);
        assertEquals(view.id, token.getStudentId());
        assertEquals(view.admin, body.admin);
    }

    private ResultActions getStudentById(Integer id, String tokenString) throws Exception {
        return mockMvc.perform(get("/api/students/{id}", id)
                .header("Authorization", bearerWithToken(tokenString)));
    }

    private List<Student> getStudentsByEmail(String[] emails) throws Exception {
        return getBody(mockMvc.perform(post("/api/students/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(emails)))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private LoginResponse authAndGetResponseBody(String tokenString) throws Exception {
        return getBody(auth(tokenString).andExpect(status().isOk()), LoginResponse.class);
    }

    private ResultActions auth(String tokenString) throws Exception {
        return mockMvc.perform(post("/api/students/auth")
                .header("Authorization", bearerWithToken(tokenString)));
    }

    private ResultActions changePassword(String password, String newPassword, int id, String token) throws Exception {
        ChangePasswordUseCase.Request request = new ChangePasswordUseCase.Request(id, password, newPassword);
        return mockMvc.perform(patch("/api/students/{id}/password", id)
                .header("Authorization", bearerWithToken(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(request)));
    }

    private void signUp10StudentsAnd4Admins() {
        range(0, 10).forEach(i -> signUp("student" + i, "student" + i + "@example.com", "password"));
        range(0, 4).forEach(i -> signUpAdmin("admin" + i, "admin" + i + "@example.com", "password"));
    }

    private ResultActions deleteStudent(int studentId) throws Exception {
        return mockMvc.perform(withAdminToken(delete("/api/students/{studentId}", studentId)));
    }
}
