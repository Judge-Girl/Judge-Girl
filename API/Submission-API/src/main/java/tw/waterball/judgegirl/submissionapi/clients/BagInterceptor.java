package tw.waterball.judgegirl.submissionapi.clients;

import tw.waterball.judgegirl.primitives.submission.Bag;

/**
 * Intercept a submit-code request that being issued to put more message into the bag.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface BagInterceptor {
    void intercept(Bag bag);
}