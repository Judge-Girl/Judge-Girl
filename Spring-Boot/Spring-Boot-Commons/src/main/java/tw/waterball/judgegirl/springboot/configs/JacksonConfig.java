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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;

import java.io.IOException;
import java.util.Date;

import static tw.waterball.judgegirl.submissionapi.views.VerdictView.toEntity;
import static tw.waterball.judgegirl.submissionapi.views.VerdictView.toViewModel;

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
                .deserializers(new VerdictIssuedEventDeserializer())
                .serializers(new VerdictIssuedEventSerializer());
    }
}

class VerdictIssuedEventSerializer extends JsonObjectSerializer<VerdictIssuedEvent> {
    @Override
    public Class<VerdictIssuedEvent> handledType() {
        return VerdictIssuedEvent.class;
    }

    @Override
    protected void serializeObject(VerdictIssuedEvent value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeNumberField("problemId", value.getProblemId());
        jgen.writeStringField("problemTitle", value.getProblemTitle());
        jgen.writeNumberField("studentId", value.getStudentId());
        jgen.writeStringField("submissionId", value.getSubmissionId());
        jgen.writeNumberField("submissionTime", value.getSubmissionTime().getTime());
        jgen.writeObjectField("verdict", toViewModel(value.getVerdict()));
        jgen.writeObjectField("submissionBag", value.getSubmissionBag());
    }
}

class VerdictIssuedEventDeserializer extends JsonObjectDeserializer<VerdictIssuedEvent> {
    @Override
    public Class<VerdictIssuedEvent> handledType() {
        return VerdictIssuedEvent.class;
    }

    @Override
    protected VerdictIssuedEvent deserializeObject(JsonParser jsonParser, DeserializationContext context, ObjectCodec codec, JsonNode tree) throws IOException {
        int problemId = tree.get("problemId").asInt();
        String problemTitle = tree.get("problemTitle").asText();
        int studentId = tree.get("studentId").asInt();
        String submissionId = tree.get("submissionId").asText();
        Date submissionTime = new Date(tree.get("submissionTime").asLong());
        Bag submissionBag = codec.treeToValue(tree.get("submissionBag"), Bag.class);
        VerdictView verdictView = codec.treeToValue(tree.get("verdict"), VerdictView.class);
        return new VerdictIssuedEvent(problemId, problemTitle, studentId, submissionId,
                toEntity(verdictView), submissionTime, submissionBag);
    }
}
