package tw.waterball.judgegirl.studentservice.domain.exceptions;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
public class StudentPasswordIncorrectException extends RuntimeException {
    public StudentPasswordIncorrectException() {
        super();
    }

    public StudentPasswordIncorrectException(String message) {
        super(message);
    }

    public StudentPasswordIncorrectException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudentPasswordIncorrectException(Throwable cause) {
        super(cause);
    }

    protected StudentPasswordIncorrectException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
