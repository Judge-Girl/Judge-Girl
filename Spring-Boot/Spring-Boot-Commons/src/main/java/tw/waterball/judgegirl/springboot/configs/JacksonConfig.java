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

package tw.waterball.judgegirl.springboot.configs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import tw.waterball.judgegirl.primitives.submission.verdict.Judge;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.submissionapi.views.ReportView;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
public class JacksonConfig {
    public static final ObjectMapper OBJECT_MAPPER;

    static {
        var objectMapperBuilder = new Jackson2ObjectMapperBuilder();
        new JacksonConfig().jsonCustomizer().customize(objectMapperBuilder);
        OBJECT_MAPPER = objectMapperBuilder.build();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnUnknownProperties(false)
                .featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .deserializers(new VerdictDeserializer());
    }
}


class VerdictDeserializer extends JsonObjectDeserializer<Verdict> {
    @Override
    public Class<Verdict> handledType() {
        return Verdict.class;
    }

    @Override
    protected Verdict deserializeObject(JsonParser jsonParser, DeserializationContext context, ObjectCodec codec, JsonNode tree) throws IOException {
        Verdict verdict;

        Date issueTime = new Date(tree.get("issueTime").asLong());
        if (tree.has("compileErrorMessage")) {
            String compileErrorMessage = tree.get("compileErrorMessage").asText();
            verdict = Verdict.compileError(compileErrorMessage, issueTime);
        } else {
            List<Judge> judges = asList(codec.treeToValue(tree.get("judges"), Judge[].class));
            verdict = new Verdict(judges, issueTime);
        }
        ReportView reportView = codec.treeToValue(tree.get("report"), ReportView.class);
        verdict.setReport(reportView.toEntity());
        return verdict;
    }
}