package tw.waterball.judgegirl.primitives.submission;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class SubmissionException extends RuntimeException {
    protected String name;

    public SubmissionException(String name) {
        this.name = name;
    }

    public SubmissionException(String name, String message) {
        super(message);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
