package tw.waterball.judgegirl.studentservice.domain.exceptions;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
public class StudentEmailNotFoundException extends RuntimeException {
    public StudentEmailNotFoundException() {
        super();
    }

    public StudentEmailNotFoundException(String message) {
        super(message);
    }

    public StudentEmailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudentEmailNotFoundException(Throwable cause) {
        super(cause);
    }

    protected StudentEmailNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
