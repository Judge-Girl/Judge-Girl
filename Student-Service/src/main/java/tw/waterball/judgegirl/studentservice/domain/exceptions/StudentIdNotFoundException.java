package tw.waterball.judgegirl.studentservice.domain.exceptions;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
public class StudentIdNotFoundException extends RuntimeException{
    public StudentIdNotFoundException() {
        super();
    }

    public StudentIdNotFoundException(String message) {
        super(message);
    }

    public StudentIdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public StudentIdNotFoundException(Throwable cause) {
        super(cause);
    }

    protected StudentIdNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
