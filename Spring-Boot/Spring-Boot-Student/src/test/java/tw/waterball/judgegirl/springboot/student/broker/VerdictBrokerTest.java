package tw.waterball.judgegirl.springboot.student.broker;

import com.github.fridujo.rabbitmq.mock.MockConnectionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import tw.waterball.judgegirl.entities.submission.Bag;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.springboot.student.SpringBootStudentApplication;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import static tw.waterball.judgegirl.entities.stubs.SubmissionStubBuilder.submission;
import static tw.waterball.judgegirl.springboot.student.broker.VerdictBroker.BAG_KEY_STOMP_ADDITIONAL_DESTINATIONS;

@ActiveProfiles(value = {Profiles.JWT, Profiles.AMQP})
@ContextConfiguration(classes = SpringBootStudentApplication.class)
class VerdictBrokerTest extends AbstractSpringBootTest {

    @Autowired
    VerdictBroker verdictBroker;

    @Autowired
    VerdictPublisher verdictPublisher;

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
                .AC(10, 10, 100));
        verdictBroker.onHandlingCompletion$.doWait(3000);
    }


    private void publishVerdict(Submission submission) {
        verdictPublisher.publish(new VerdictIssuedEvent(30, "title", 30, submission.getId(),
                submission.mayHaveVerdict().orElseThrow(),
                submission.getSubmissionTime(),
                new Bag(BAG_KEY_STOMP_ADDITIONAL_DESTINATIONS, "/additional/destinations1, /additional/destination2")));
    }

}