package tw.waterball.judgegirl.submission.domain.usecases;

import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@FunctionalInterface
public interface VerdictIssuedEventHandler {
    void handle(VerdictIssuedEvent event);
}
