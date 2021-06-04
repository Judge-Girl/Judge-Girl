package tw.waterball.judgegirl.springboot.academy.amqp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.academy.domain.usecases.VerdictIssuedEventListener;
import tw.waterball.judgegirl.commons.utils.NotifyWaitLock;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.springboot.profiles.productions.Amqp;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Amqp
@Slf4j
@AllArgsConstructor
@Component
public class VerdictIssuedEventHandler {
    private final List<VerdictIssuedEventListener> listeners;

    // This is for test-purpose, test script can wait for this lock until the next onIssueVerdict()
    public final NotifyWaitLock onHandlingCompletion$ = new NotifyWaitLock();

    @RabbitListener(queues = "${judge-girl.amqp.exam-service-queue}")
    public void listen(VerdictIssuedEvent event) {
        log.info("Handle: {}", event);
        listeners.forEach(l -> l.onVerdictIssued(event));
        onHandlingCompletion$.doNotifyAll();
    }
}
