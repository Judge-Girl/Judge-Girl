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

package tw.waterball.judgegirl.springboot.student.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.profiles.productions.Prod;
import tw.waterball.judgegirl.springboot.student.exceptions.PasswordIncorrectException;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Prod
@Component
public class JudgeGirlLegacyStudentApiAdapter implements LegacyStudentAPI {
    private final String baseUrl;
    private final String apiKey;
    private RestTemplate restTemplate;

    public JudgeGirlLegacyStudentApiAdapter(@Value("${judge-girl.student-service.legacy-judge-girl.base-url}")
                                                    String baseUrl,
                                            @Value("${judge-girl.student-service.legacy-judge-girl.api-key}")
                                                    String apiKey,
                                            RestTemplate restTemplate) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.restTemplate = restTemplate;
    }

    @Override
    public int authenticate(String account, String password) throws NotFoundException, PasswordIncorrectException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Api-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(
                String.format("user=%s&password=%s", account, password), headers);
        try {
            ResponseEntity<LegacyAuthResponse> response = restTemplate.postForEntity(baseUrl + "/api/auth", entity, LegacyAuthResponse.class);
            return requireNonNull(response.getBody()).userId;
        } catch (HttpClientErrorException.NotFound err) {
            throw NotFoundException.resource("Student").id(account);
        } catch (HttpClientErrorException.BadRequest err) {
            throw new PasswordIncorrectException();
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public Optional<Student> getStudentByAccount(String account) {
        return Optional.empty();
    }

    @Override
    public Optional<Student> getStudentById(int studentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Api-key", apiKey);
        try {
            ResponseEntity<LegacyAuthResponse> response = restTemplate.exchange(baseUrl + "/api/user/" + studentId,
                    HttpMethod.GET, new HttpEntity<>(headers), LegacyAuthResponse.class);
            LegacyAuthResponse legacyAuthResponse = requireNonNull(response.getBody());
            return Optional.of(new Student(legacyAuthResponse.userId, legacyAuthResponse.lgn,
                    null /*Name is not supported in the current version*/));
        } catch (HttpClientErrorException.NotFound err) {
            throw new NotFoundException(studentId, "Student");
        } catch (Exception err) {
            throw new RuntimeException(err);
        }
    }

    @ToString
    @Setter
    private static class LegacyAuthResponse {
        @JsonProperty("uid")
        int userId;
        String lgn;

        @JsonProperty("class")
        String clazz;
    }
}
