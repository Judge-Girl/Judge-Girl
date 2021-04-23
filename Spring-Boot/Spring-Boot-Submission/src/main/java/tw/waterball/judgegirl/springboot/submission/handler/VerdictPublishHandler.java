package tw.waterball.judgegirl.springboot.submission.handler;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.helpers.EventBus;
import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.submissionapi.clients.VerdictPublisher;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@AllArgsConstructor
@Component
public class VerdictPublishHandler implements EventBus.Handler {
    private final VerdictPublisher verdictPublisher;

    @Override
    public void handle(Object event) {
        if (event instanceof VerdictIssuedEvent) {
            verdictPublisher.publish((VerdictIssuedEvent) event);
        }
    }
}
