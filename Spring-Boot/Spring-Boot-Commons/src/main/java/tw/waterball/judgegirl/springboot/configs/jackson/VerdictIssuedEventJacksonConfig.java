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
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;

import java.io.IOException;
import java.util.Date;

import static tw.waterball.judgegirl.submissionapi.views.VerdictView.toEntity;
import static tw.waterball.judgegirl.submissionapi.views.VerdictView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
public class VerdictIssuedEventJacksonConfig {
    public final static JsonDeserializer<VerdictIssuedEvent> DESERIALIZER = new JsonObjectDeserializer<>() {
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
    };

    public final static JsonSerializer<VerdictIssuedEvent> SERIALIZER = new JsonObjectSerializer<>() {
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
    };

    @Bean
    public JsonDeserializer<VerdictIssuedEvent> verdictIssuedEventJsonDeserializer() {
        return DESERIALIZER;
    }

    @Bean
    public JsonSerializer<VerdictIssuedEvent> verdictIssuedEventJsonSerializer() {
        return SERIALIZER;
    }
}
