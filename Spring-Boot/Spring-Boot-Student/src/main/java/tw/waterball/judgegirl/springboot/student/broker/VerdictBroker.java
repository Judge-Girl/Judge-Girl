package tw.waterball.judgegirl.springboot.student.broker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.utils.NotifyWaitLock;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.springboot.profiles.productions.Amqp;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static tw.waterball.judgegirl.springboot.student.broker.AmqpConfiguration.BROKER_QUEUE;
import static tw.waterball.judgegirl.springboot.student.broker.WebSocketConfiguration.STOMP_ROOT_DESTINATION_PREFIX;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Amqp
@Slf4j
@Component
@AllArgsConstructor
public class VerdictBroker {
    // broadcast to additional destinations (split by commas)
    public static final String BAG_KEY_ADDITIONAL_DESTINATIONS = "broker-additional-destinations";
    private final SimpMessagingTemplate simpMessaging;

    public final NotifyWaitLock onHandlingCompletion$ = new NotifyWaitLock();

    @RabbitListener(queues = "#{" + BROKER_QUEUE + ".name}")
    public void listen(VerdictIssuedEvent event) {
        String studentDestination = String.format("%s/students/%d/verdicts",
                STOMP_ROOT_DESTINATION_PREFIX, event.getStudentId());
        String problemDestination = String.format("%s/problems/%d/verdicts",
                STOMP_ROOT_DESTINATION_PREFIX, event.getProblemId());
        List<String> destinations = new ArrayList<>(asList(studentDestination, problemDestination));
        Bag bag = event.getSubmissionBag();
        destinations.addAll(asList(
                bag.getAsString(BAG_KEY_ADDITIONAL_DESTINATIONS)
                        .map(s -> s.split("\\s*,\\s*")).orElseGet(() -> new String[0])));
        log.info("Event: {}, Broadcast to => {}", event, String.join(", ", destinations));
        destinations.forEach(destination -> simpMessaging.convertAndSend(destination, event));

        onHandlingCompletion$.doNotifyAll();
    }
}
