package tw.waterball.judgegirl.springboot.student.broker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.utils.NotifyWaitLock;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent;
import tw.waterball.judgegirl.springboot.profiles.productions.Amqp;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static tw.waterball.judgegirl.springboot.student.broker.AmqpConfiguration.LIVE_SUBMISSIONS_BROKER_QUEUE;
import static tw.waterball.judgegirl.springboot.student.broker.WebSocketConfiguration.STOMP_ROOT_DESTINATION_PREFIX;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Amqp
@Slf4j
@Component
@AllArgsConstructor
public class LiveSubmissionsBroker extends AbstractBroker {
    private final SimpMessagingTemplate simpMessaging;

    public final NotifyWaitLock onHandlingCompletion$ = new NotifyWaitLock();

    @RabbitListener(queues = "#{" + LIVE_SUBMISSIONS_BROKER_QUEUE + ".name}")
    public void listen(LiveSubmissionEvent event) {
        String studentDestination = String.format("%s/students/%d/submissions",
                STOMP_ROOT_DESTINATION_PREFIX, event.getStudentId());
        String problemDestination = String.format("%s/problems/%d/submissions",
                STOMP_ROOT_DESTINATION_PREFIX, event.getProblemId());

        List<String> destinations = new ArrayList<>(asList(studentDestination, problemDestination));
        Bag bag = event.getSubmissionBag();

        destinations.addAll(getAdditionalDestinationsFromBag(bag, "submissions"));
        log.trace("[Consume: {}] broker-destinations=[{}]", event.getName(), String.join(", ", destinations));
        destinations.forEach(destination -> simpMessaging.convertAndSend(destination, event));

        onHandlingCompletion$.doNotifyAll();
    }

}
