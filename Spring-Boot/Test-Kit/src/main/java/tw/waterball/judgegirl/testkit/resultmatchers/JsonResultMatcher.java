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

package tw.waterball.judgegirl.testkit.resultmatchers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class JsonResultMatcher implements ResultMatcher {
    private Object expectedObj;
    private ObjectMapper objectMapper;

    public JsonResultMatcher() {
    }

    public static JsonResultMatcher.Builder objectMapper(ObjectMapper objectMapper) {
        JsonResultMatcher matcher = new JsonResultMatcher();
        return matcher.new Builder();
    }

    @Override
    public void match(MvcResult result) throws Exception {
        assertEquals(MediaType.APPLICATION_JSON_VALUE, result.getResponse().getContentType(),
                "Expect Content-Type: " + MediaType.APPLICATION_JSON_VALUE);

        String json = result.getResponse().getContentAsString();
        String expectedJson = objectMapper.writeValueAsString(expectedObj);

        JsonNode actualJsonTree = objectMapper.readTree(json);
        JsonNode expectedJsonTree = objectMapper.readTree(expectedJson);

        assertEquals(expectedJsonTree, actualJsonTree);
    }

    public class Builder {
        public JsonResultMatcher compareWith(Object expectedObj) {
            JsonResultMatcher.this.expectedObj = expectedObj;
            return JsonResultMatcher.this;
        }
    }
}
