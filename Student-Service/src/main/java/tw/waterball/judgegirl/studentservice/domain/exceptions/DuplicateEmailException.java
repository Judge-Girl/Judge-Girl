package tw.waterball.judgegirl.studentservice.domain.exceptions;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super();
    }

    public DuplicateEmailException(String message) {
        super(message);
    }

    public DuplicateEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateEmailException(Throwable cause) {
        super(cause);
    }

    protected DuplicateEmailException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
