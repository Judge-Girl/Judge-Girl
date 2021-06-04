package tw.waterball.judgegirl.springboot.student.broker;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.springboot.profiles.productions.Amqp;

import java.util.UUID;

import static java.lang.String.format;
import static org.springframework.amqp.core.BindingBuilder.bind;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Amqp
@Configuration
public class AmqpConfiguration {
    public static final String VERDICT_BROKER_QUEUE = "verdictBrokerQueue";
    public static final String LIVE_SUBMISSIONS_BROKER_QUEUE = "liveSubmissionsBrokerQueue";

    @Bean(VERDICT_BROKER_QUEUE)
    public Queue verdictBrokerQueue(@Value("${judge-girl.amqp.broker-queue-format}") String brokerQueueNameFormat) {
        // (*) Broker-Services must not share the same queue. Use UUID to distinguish their queues.
        String queueName = format(brokerQueueNameFormat, "verdict", UUID.randomUUID().toString());
        return new Queue(queueName, false);
    }

    @Bean(LIVE_SUBMISSIONS_BROKER_QUEUE)
    public Queue liveSubmissionsBrokerQueue(@Value("${judge-girl.amqp.broker-queue-format}") String brokerQueueNameFormat) {
        // (*) Broker-Services must not share the same queue. Use UUID to distinguish their queues.
        String queueName = format(brokerQueueNameFormat, "live-submissions", UUID.randomUUID().toString());
        return new Queue(queueName, false);
    }

    @Bean
    public TopicExchange submissionsExchangeName(@Value("${judge-girl.amqp.submissions-exchange-name}") String submissionsExchangeName) {
        return new TopicExchange(submissionsExchangeName);
    }

    @Bean
    public Binding bindVerdictIssuedEventToVerdictBrokerQueue(
            @Value("${judge-girl.amqp.verdict-issued-routing-key-format}")
                    String verdictIssuedRoutingKeyFormat,
            @Qualifier(VERDICT_BROKER_QUEUE) Queue verdictBrokerQueue,
            @Qualifier("submissionsExchangeName") TopicExchange submissionsExchange) {
        return bind(verdictBrokerQueue)
                .to(submissionsExchange)
                .with(format(verdictIssuedRoutingKeyFormat, "*"));
    }
    
    @Bean
    public Binding bindLiveSubmissionEventsToLiveSubmissionBrokerQueue(
            @Value("${judge-girl.amqp.live-submissions-routing-key}")
                    String liveSubmissionsRoutingKey,
            @Qualifier(LIVE_SUBMISSIONS_BROKER_QUEUE) Queue liveSubmissionsBrokerQueue,
            @Qualifier("submissionsExchangeName") TopicExchange submissionsExchange) {
        return bind(liveSubmissionsBrokerQueue)
                .to(submissionsExchange)
                .with(liveSubmissionsRoutingKey);
    }


}
