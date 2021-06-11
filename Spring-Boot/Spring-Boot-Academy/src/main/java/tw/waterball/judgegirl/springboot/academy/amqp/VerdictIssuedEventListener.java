package tw.waterball.judgegirl.springboot.academy.amqp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.academy.domain.usecases.VerdictIssuedEventHandler;
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
public class VerdictIssuedEventListener {
    private final List<VerdictIssuedEventHandler> handlers;

    // This is for test-purpose, test script can wait for this lock until the next onIssueVerdict()
    public final NotifyWaitLock onHandlingCompletion$ = new NotifyWaitLock();

    @RabbitListener(queues = "${judge-girl.amqp.academy-service-queue}")
    public void listen(VerdictIssuedEvent event) {
        log.trace("[Consume: {}] {}", event.getName(), event);
        handlers.forEach(l -> l.handle(event));
        onHandlingCompletion$.doNotifyAll();
    }
}
