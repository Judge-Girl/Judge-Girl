package tw.waterball.judgegirl.springboot.configs.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import org.springframework.boot.jackson.JsonObjectDeserializer;
import org.springframework.boot.jackson.JsonObjectSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent;

import java.io.IOException;
import java.util.Date;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
public class LiveSubmissionEventJacksonConfig {
    public static final JsonDeserializer<LiveSubmissionEvent> DESERIALIZER = new JsonObjectDeserializer<>() {
        @Override
        public Class<LiveSubmissionEvent> handledType() {
            return LiveSubmissionEvent.class;
        }

        @Override
        protected LiveSubmissionEvent deserializeObject(JsonParser jsonParser, DeserializationContext context, ObjectCodec codec, JsonNode tree) throws IOException {
            int problemId = tree.get("problemId").asInt();
            String languageEnvName = tree.get("languageEnvName").asText();
            int studentId = tree.get("studentId").asInt();
            String submissionId = tree.get("submissionId").asText();
            Date submissionTime = new Date(tree.get("submissionTime").asLong());
            Bag bag = codec.treeToValue(tree.get("submissionBag"), Bag.class);
            return new LiveSubmissionEvent(problemId, languageEnvName, studentId, submissionId, submissionTime, bag);
        }
    };

    public static final JsonSerializer<LiveSubmissionEvent> SERIALIZER = new JsonObjectSerializer<>() {
        @Override
        public Class<LiveSubmissionEvent> handledType() {
            return LiveSubmissionEvent.class;
        }

        @Override
        protected void serializeObject(LiveSubmissionEvent value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeNumberField("problemId", value.getProblemId());
            jgen.writeStringField("languageEnvName", value.getLanguageEnvName());
            jgen.writeNumberField("studentId", value.getStudentId());
            jgen.writeStringField("submissionId", value.getSubmissionId());
            jgen.writeNumberField("submissionTime", value.getSubmissionTime().getTime());
            jgen.writeObjectField("submissionBag", value.getSubmissionBag());
        }
    };

    @Bean
    public JsonDeserializer<LiveSubmissionEvent> liveSubmissionEventJsonDeserializer() {
        return DESERIALIZER;
    }

    @Bean
    public JsonSerializer<LiveSubmissionEvent> liveSubmissionEventJsonSerializer() {
        return SERIALIZER;
    }

}
