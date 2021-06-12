package tw.waterball.judgegirl.primitives.exam;

import tw.waterball.judgegirl.primitives.submission.SubmissionException;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class NoSubmissionQuotaException extends SubmissionException {
    public static final String NAME = "no-submission-quota";
    private final int submissionQuota;

    public NoSubmissionQuotaException(int submissionQuota) {
        super(NAME);
        this.submissionQuota = submissionQuota;
    }

    public int getSubmissionQuota() {
        return submissionQuota;
    }
}
