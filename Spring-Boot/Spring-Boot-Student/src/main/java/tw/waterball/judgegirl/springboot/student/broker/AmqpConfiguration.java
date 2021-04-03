package tw.waterball.judgegirl.springboot.student.broker;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.springboot.profiles.productions.Amqp;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Amqp
@Configuration
public class AmqpConfiguration {
    public static final String BROKER_QUEUE = "brokerQueue";

    @Bean
    public Queue brokerQueue(@Value("${judge-girl.amqp.broker-queue}") String brokerQueueName) {
        return new Queue(brokerQueueName, true);
    }

    @Bean
    public TopicExchange verdictExchange(@Value("${judge-girl.amqp.verdict-exchange-name}") String verdictExchangeName) {
        return new TopicExchange(verdictExchangeName);
    }

    @Bean
    public Binding binding(@Value("${judge-girl.amqp.verdict-issued-routing-key-format}")
                                   String verdictIssuedRoutingKeyFormat,
                           @Qualifier(BROKER_QUEUE) Queue queue,
                           @Qualifier("verdictExchange") TopicExchange exchange) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(String.format(verdictIssuedRoutingKeyFormat, "*"));
    }

}
