package tw.waterball.judgegirl.examservice.domain.usecases;

import tw.waterball.judgegirl.entities.submission.verdict.VerdictIssuedEvent;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface VerdictIssuedEventListener {
    void onVerdictIssued(VerdictIssuedEvent event);
}
