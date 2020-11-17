package tw.waterball.judgegirl.submissionservice.domain.usecases.exceptions;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class SubmissionThrottlingException extends IllegalStateException {

    public SubmissionThrottlingException(long secondsToWait) {
        super("You should wait about " + secondsToWait + " seconds for the next submission.");
    }

}
