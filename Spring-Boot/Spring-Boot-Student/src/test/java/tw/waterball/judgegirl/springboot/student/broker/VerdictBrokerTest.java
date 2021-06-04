package tw.waterball.judgegirl.springboot.student.broker;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.SpringBootStudentApplication;
import tw.waterball.judgegirl.submissionapi.clients.EventPublisher;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static tw.waterball.judgegirl.primitives.stubs.SubmissionStubBuilder.submission;
import static tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent.liveSubmission;
import static tw.waterball.judgegirl.springboot.student.broker.BrokerProperties.BAG_KEY_ADDITIONAL_DESTINATIONS;

@ActiveProfiles(value = {Profiles.JWT, Profiles.AMQP})
@ContextConfiguration(classes = SpringBootStudentApplication.class)
class VerdictBrokerTest extends AbstractSpringBootTest {

    @Autowired
    VerdictBroker verdictBroker;

    @Autowired
    LiveSubmissionsBroker liveSubmissionsBroker;

    @Autowired
    EventPublisher eventPublisher;

    @MockBean
    SimpMessagingTemplate simpMessagingTemplate;

    String[] additionalDestinations = new String[]{"/additional/destinations1", "/additional/destination2"};

    @Configuration
    public static class TestConfig {
        @Bean
        @Primary
        public ConnectionFactory mockRabbitMqConnectionFactory() {
            return new CachingConnectionFactory(new MockConnectionFactory());
        }
    }

    @Test
    void WhenPublishVerdict_ShouldBroadcast() {
        publishVerdict(submission("A")
                .AC(10, 10, 100).build(100, 200, "JAVA"));
        verdictBroker.onHandlingCompletion$.doWait(3000);

        verify(simpMessagingTemplate).convertAndSend(eq("/topic/problems/200/verdicts"), isA(VerdictIssuedEvent.class));
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/students/100/verdicts"), isA(VerdictIssuedEvent.class));

        for (String destination : additionalDestinations) {
            verify(simpMessagingTemplate)
                    .convertAndSend(eq("/topic" + destination + "/verdicts"), isA(VerdictIssuedEvent.class));
        }
    }

    @Test
    void WhenPublishLiveSubmission_ShouldBroadcast() {
        publishLiveSubmissions(submission("A").CE(100).build(1, 2, "C"));
        liveSubmissionsBroker.onHandlingCompletion$.doWait(3000);

        verify(simpMessagingTemplate).convertAndSend(eq("/topic/problems/2/submissions"), isA(LiveSubmissionEvent.class));
        verify(simpMessagingTemplate).convertAndSend(eq("/topic/students/1/submissions"), isA(LiveSubmissionEvent.class));

        for (String destination : additionalDestinations) {
            verify(simpMessagingTemplate)
                    .convertAndSend(eq("/topic" + destination + "/submissions"), isA(LiveSubmissionEvent.class));
        }
    }

    private void publishVerdict(Submission submission) {
        eventPublisher.publish(new VerdictIssuedEvent(submission.getProblemId(), "title", submission.getStudentId(), submission.getId(),
                submission.mayHaveVerdict().orElseThrow(),
                submission.getSubmissionTime(),
                new Bag(BAG_KEY_ADDITIONAL_DESTINATIONS, String.join(", ", additionalDestinations))));
    }


    private void publishLiveSubmissions(Submission submission) {
        submission.setBag(new Bag(BAG_KEY_ADDITIONAL_DESTINATIONS, String.join(", ", additionalDestinations)));
        eventPublisher.publish(liveSubmission(submission));
    }

}