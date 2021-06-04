package tw.waterball.judgegirl.springboot.submission.amqp;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.helpers.EventBus;
import tw.waterball.judgegirl.primitives.submission.events.LiveSubmissionEvent;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.submissionapi.clients.EventPublisher;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@AllArgsConstructor
@Component
public class VerdictPublishHandler implements EventBus.Handler {
    private final EventPublisher eventPublisher;

    @Override
    public void handle(Object event) {
        if (event instanceof VerdictIssuedEvent) {
            eventPublisher.publish((VerdictIssuedEvent) event);
        }
        if (event instanceof LiveSubmissionEvent) {
            eventPublisher.publish((LiveSubmissionEvent) event);
        }
    }
}
