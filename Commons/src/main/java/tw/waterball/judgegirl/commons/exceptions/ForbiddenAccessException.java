package tw.waterball.judgegirl.commons.exceptions;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException() {
        super("Forbidden.");
    }

    public ForbiddenAccessException(String message) {
        super(message);
    }

    public ForbiddenAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenAccessException(Throwable cause) {
        super(cause);
    }

    public ForbiddenAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
