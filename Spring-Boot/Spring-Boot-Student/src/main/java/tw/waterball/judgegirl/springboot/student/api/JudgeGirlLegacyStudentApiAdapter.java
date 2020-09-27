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
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tw.waterball.judgegirl.commons.entities.Student;
import tw.waterball.judgegirl.commons.profiles.productions.Prod;
import tw.waterball.judgegirl.commons.utils.RestTemplates;
import tw.waterball.judgegirl.springboot.student.exceptions.AccountNotFoundException;
import tw.waterball.judgegirl.springboot.student.exceptions.PasswordIncorrectException;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.Objects.requireNonNull;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Prod
@Component
public class JudgeGirlLegacyStudentApiAdapter implements LegacyStudentAPI {
    private final static String host;
    private final static String apiKey;

    static {
        ResourceBundle properties = ResourceBundle.getBundle("judge-girl", Locale.ROOT);
        host = properties.getString("legacy-judge-girl-host");
        apiKey = properties.getString("legacy-judge-girl-api-key");
    }

    private RestTemplate restTemplate;

    public JudgeGirlLegacyStudentApiAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate restTemplate = RestTemplates.ignoreCertificationRestTemplate();

        JudgeGirlLegacyStudentApiAdapter judgeGirlLegacyStudentApiAdapter =
                new JudgeGirlLegacyStudentApiAdapter(restTemplate);

        ResourceBundle properties = ResourceBundle.getBundle("judge-girl", Locale.ROOT);
        String account = properties.getString("test-account");
        String password = properties.getString("test-password");

        int studentId = judgeGirlLegacyStudentApiAdapter.authenticate(account, password);
        Student student = judgeGirlLegacyStudentApiAdapter.getStudentById(studentId).get();
        System.out.println(student);
    }

    @Override
    public int authenticate(String account, String password) throws AccountNotFoundException, PasswordIncorrectException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Api-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(
                String.format("user=%s&password=%s", account, password), headers);
        try {
            ResponseEntity<LegacyAuthResponse> response = restTemplate.postForEntity(host + "/api/auth", entity, LegacyAuthResponse.class);
            return requireNonNull(response.getBody()).userId;
        } catch (HttpClientErrorException.NotFound err) {
            throw new AccountNotFoundException(account);
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
            ResponseEntity<LegacyAuthResponse> response = restTemplate.exchange(host + "/api/user/" + studentId,
                    HttpMethod.GET, new HttpEntity<>(headers), LegacyAuthResponse.class);
            LegacyAuthResponse legacyAuthResponse = requireNonNull(response.getBody());
            return Optional.of(new Student(legacyAuthResponse.userId, legacyAuthResponse.lgn,
                    null /*Name is not supported in the current version*/));
        } catch (HttpClientErrorException.NotFound err) {
            throw new StudentNotFoundException(studentId);
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
