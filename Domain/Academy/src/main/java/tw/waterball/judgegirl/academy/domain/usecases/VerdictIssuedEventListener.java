package tw.waterball.judgegirl.academy.domain.usecases;

import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuedEvent;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface VerdictIssuedEventListener {
    void onVerdictIssued(VerdictIssuedEvent event);
}
