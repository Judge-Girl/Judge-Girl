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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tw.waterball.judgegirl.testkit.jupiter.ReplaceUnderscoresWithCamelCasesDisplayNameGenerators;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @SneakyThrows
    public String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    @SneakyThrows
    public <T> T fromJson(String json, Class<T> type) {
        return objectMapper.readValue(json, type);
    }

    @SneakyThrows
    protected <T> T getBody(ResultActions actions, Class<T> type) {
        return fromJson(actions
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                type);
    }

    @SneakyThrows
    protected <T> List<T> getBody(ResultActions actions, TypeReference<List<T>> type) {
        return fromJson(actions
                .andReturn()
                .getResponse()
                .getContentAsString(), type);
    }

    @SneakyThrows
    public <T> List<T> fromJson(String json, TypeReference<List<T>> type) {
        return objectMapper.readValue(json, type);
    }

    public void assertEqualsIgnoreOrder(Collection<?> expected, Collection<?> actual) {
        assertEquals(new HashSet<>(expected), new HashSet<>(actual));
    }

}
