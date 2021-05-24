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

package tw.waterball.judgegirl.testkit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.commons.utils.functional.ErrRunnable;
import tw.waterball.judgegirl.testkit.jupiter.ReplaceUnderscoresWithCamelCasesDisplayNameGenerators;
import tw.waterball.judgegirl.testkit.semantics.WithHeader;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.admin;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.student;
import static tw.waterball.judgegirl.commons.utils.HttpHeaderUtils.bearerWithToken;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(ReplaceUnderscoresWithCamelCasesDisplayNameGenerators.class)
public abstract class AbstractSpringBootTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TokenService tokenService;

    @SneakyThrows
    public String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    @SneakyThrows
    public <T> T fromJson(String json, Class<T> type) {
        return objectMapper.readValue(json, type);
    }

    protected <T> T getBody(ResultActions actions, Class<T> type) {
        return fromJson(getContentAsString(actions), type);
    }

    protected <T> List<T> getBody(ResultActions actions, TypeReference<List<T>> type) {
        return fromJson(getContentAsString(actions), type);
    }

    @SneakyThrows
    protected String getContentAsString(ResultActions actions) {
        return actions
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @SneakyThrows
    public <T> List<T> fromJson(String json, TypeReference<List<T>> type) {
        return objectMapper.readValue(json, type);
    }

    public void assertEqualsIgnoreOrder(Collection<?> expected, Collection<?> actual) {
        assertEquals(new HashSet<>(expected), new HashSet<>(actual));
    }

    protected void anotherTransaction(ErrRunnable anotherTransaction) throws Exception {
        if (TestTransaction.isActive()) {
            TestTransaction.flagForCommit();
            TestTransaction.end();
        }
        TestTransaction.start();
        anotherTransaction.run();
        TestTransaction.flagForCommit();
        TestTransaction.end();
    }
    
    protected MockHttpServletRequestBuilder withAdminToken(MockHttpServletRequestBuilder builder) {
        withToken(tokenService.createToken(admin(Integer.MAX_VALUE))).decorate(builder);
        return builder;
    }

    protected MockHttpServletRequestBuilder withStudentToken(int studentId, MockHttpServletRequestBuilder builder) {
        withToken(tokenService.createToken(student(studentId))).decorate(builder);
        return builder;
    }

    protected MockHttpServletRequestBuilder withToken(TokenService.Token token, MockHttpServletRequestBuilder builder) {
        withToken(token).decorate(builder);
        return builder;
    }

    protected WithHeader withToken(TokenService.Token token) {
        return request -> request.header("Authorization", bearerWithToken(token.getToken()));
    }


}
