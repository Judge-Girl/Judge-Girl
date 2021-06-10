package tw.waterball.judgegirl.primitives.exam;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class NoSubmissionQuotaException extends IllegalStateException {
    private final int submissionQuota;

    public NoSubmissionQuotaException(int submissionQuota) {
        this.submissionQuota = submissionQuota;
    }

    public int getSubmissionQuota() {
        return submissionQuota;
    }
}
