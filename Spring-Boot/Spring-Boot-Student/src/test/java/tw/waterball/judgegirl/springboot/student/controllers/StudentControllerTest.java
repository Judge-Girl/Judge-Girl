/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.springboot.student.controllers;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.api.LegacyStudentAPI;
import tw.waterball.judgegirl.springboot.student.exceptions.PasswordIncorrectException;
import tw.waterball.judgegirl.springboot.token.TokenInvalidException;
import tw.waterball.judgegirl.springboot.token.TokenService;

import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentController.class)
class StudentControllerTest {
    @MockBean
    LegacyStudentAPI legacyStudentAPI;

    @MockBean
    TokenService tokenService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void givenCorrectCredential_whenLogin_shouldGetToken() throws Exception {
        final int ID = 1;
        final String TOKEN = "token", ACCOUNT = "account", PASSWORD = "password";

        when(legacyStudentAPI.authenticate(ACCOUNT, PASSWORD)).thenReturn(ID);
        when(tokenService.createToken(any())).thenReturn(
                TokenService.Token.ofStudent(ID, TOKEN, new Date(Long.MAX_VALUE)));

        mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(String.format("account=%s&password=%s", ACCOUNT, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").value(TOKEN));
    }

    @Test()
    void givenIncorrectStudentId_whenLogin_shouldRespondNotFound() throws Exception {
        final String ACCOUNT = "account";
        Mockito.doThrow(NotFoundException.resource("account").id(ACCOUNT))
                .when(legacyStudentAPI).authenticate(eq(ACCOUNT), anyString());

        mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(String.format("account=%s&password=%s", ACCOUNT, "pwd")))
                .andExpect(status().isNotFound());
    }

    @Test()
    void givenIncorrectPassword_whenLogin_shouldReturnBadRequest() throws Exception {
        final String PASSWORD = "password";

        Mockito.doThrow(new PasswordIncorrectException())
                .when(legacyStudentAPI).authenticate(anyString(), eq(PASSWORD));

        mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(String.format("account=%s&password=%s", "id", PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenCorrectIdAndToken_whenGetStudent_shouldRespondSuccessfully() throws Exception {
        final int ID = 1;
        final String ACCOUNT = "id";
        final String TOKEN = "token";
        final Student stub = new Student(ID, ACCOUNT, "name");
        when(tokenService.parseAndValidate(TOKEN)).thenReturn(
                TokenService.Token.ofStudent(ID, TOKEN, new Date(Long.MAX_VALUE)));
        when(legacyStudentAPI.getStudentById(ID)).thenReturn(Optional.of(stub));

        mockMvc.perform(get("/api/students/{studentId}", ID)
                .header("Authorization", "bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(ID))
                .andExpect(jsonPath("name").value(stub.getName()));
    }

    @Test
    void givenIncorrectToken_whenGetStudent_shouldRespondUnauthorized() throws Exception {
        final int ID = 1;
        when(tokenService.parseAndValidate(any())).thenThrow(new TokenInvalidException());

        mockMvc.perform(get("/api/students/{studentId}", ID)
                .header("Authorization", "bearer token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenNotExistStudentId_whenGetStudent_shouldRespondNotFound() throws Exception {
        final int ID = 1;
        final String TOKEN = "token";
        when(legacyStudentAPI.getStudentByAccount(anyString())).thenReturn(Optional.empty());
        when(tokenService.parseAndValidate(TOKEN)).thenReturn(
                TokenService.Token.ofStudent(ID, TOKEN, new Date(Long.MAX_VALUE)));

        mockMvc.perform(get("/api/students/{studentId}", ID)
                .header("Authorization", "bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }
}