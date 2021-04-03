package tw.waterball.judgegirl.springboot.student.broker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.submissionapi.views.VerdictIssuedEvent;

import static tw.waterball.judgegirl.springboot.student.broker.WebSocketConfiguration.DESTINATION_PREFIX;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Component
@AllArgsConstructor
public class VerdictBroker {
    private final SimpMessagingTemplate simpMessaging;

    @RabbitListener(queues = "${judge-girl.amqp.broker-queue}")
    public void listen(VerdictIssuedEvent event) {
        String destination = String.format("/%s/students/%d/verdicts",
                DESTINATION_PREFIX, event.getStudentId());
        log.info("Event: {}, Broadcast to => {}", event, destination);
        simpMessaging.convertAndSend(destination, event);
    }
}
