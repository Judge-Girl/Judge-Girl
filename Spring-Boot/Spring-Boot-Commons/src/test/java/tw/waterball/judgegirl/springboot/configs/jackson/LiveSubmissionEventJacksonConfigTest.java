package tw.waterball.judgegirl.springboot.configs.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent;
import tw.waterball.judgegirl.springboot.configs.JacksonConfig;

import static tw.waterball.judgegirl.primitives.stubs.SubmissionStubBuilder.submission;
import static tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent.liveSubmission;

class LiveSubmissionEventJacksonConfigTest {
    static final ObjectMapper objectMapper = JacksonConfig.OBJECT_MAPPER;

    @Test
    void test() throws JsonProcessingException {
        var submission = submission("A")
                .AC(1000, 2000, 100)
                .build(1, 2, "C");


        LiveSubmissionEvent before = liveSubmission(submission);
        LiveSubmissionEvent after = objectMapper.readValue(
                objectMapper.writeValueAsString(before), LiveSubmissionEvent.class);

        Assertions.assertEquals(before, after);
    }

}