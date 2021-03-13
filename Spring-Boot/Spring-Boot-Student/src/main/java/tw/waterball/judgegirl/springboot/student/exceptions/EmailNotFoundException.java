package tw.waterball.judgegirl.springboot.student.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmailNotFoundException extends RuntimeException{

    public EmailNotFoundException() {
        super();
    }

    public EmailNotFoundException(String message) {
        super(message);
    }

    public EmailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailNotFoundException(Throwable cause) {
        super(cause);
    }

    protected EmailNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
