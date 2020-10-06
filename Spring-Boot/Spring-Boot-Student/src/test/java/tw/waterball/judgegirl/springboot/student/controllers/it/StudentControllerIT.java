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

package tw.waterball.judgegirl.springboot.student.controllers.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.StudentAdapterServiceApplication;
import tw.waterball.judgegirl.springboot.student.controllers.LoginResponse;
import tw.waterball.judgegirl.springboot.token.TokenService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles({Profiles.PROD, Profiles.JWT})
@AutoConfigureMockMvc
@PropertySource("classpath:judge-girl.properties")
@ContextConfiguration(classes = StudentAdapterServiceApplication.class)
class StudentControllerIT {
    @Value("${test-account}")
    String account;

    @Value("${test-password}")
    String password;

    @Value("${test-student-id}")
    int studentId;

    @Value("${test-jwt-token}")
    String token;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TokenService tokenService;

    @DisplayName("Given an admin account of legacy judge-girl system " +
            "when login, should respond correct token.")
    @Test
    void loginLegacySystem() throws Exception {
        mockMvc.perform(post("/api/students/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content(String.format("account=%s&password=%s", account, password)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    LoginResponse loginResponse = objectMapper.readValue(
                            result.getResponse().getContentAsString(), LoginResponse.class);
                    TokenService.Token token = tokenService.parseAndValidate(loginResponse.getToken());
                    assertEquals(studentId, loginResponse.getId(), "Responded Token is not correct.");
                    assertEquals(studentId, token.getStudentId());
                    assertEquals(token.getExpiration().getTime() / 1000, loginResponse.getExpiryTime() / 1000);
                    assertTrue(System.currentTimeMillis() < token.getExpiration().getTime(),
                            "Responded Expiry Time is weird, it has already expired since it's responded.");
                });
    }

    @Test
    void auth() throws Exception {
        mockMvc.perform(post("/api/students/auth")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    LoginResponse loginResponse = objectMapper.readValue(
                            result.getResponse().getContentAsString(), LoginResponse.class);
                    TokenService.Token token = tokenService.parseAndValidate(loginResponse.getToken());
                    assertEquals(studentId, loginResponse.getId(), "Responded Token is not correct.");
                    assertEquals(studentId, token.getStudentId());
                    assertEquals(token.getExpiration().getTime() / 1000, loginResponse.getExpiryTime() / 1000);
                    assertTrue(System.currentTimeMillis() < token.getExpiration().getTime(),
                            "Responded Expiry Time is weird, it has already expired since it's responded.");
                });
    }
}