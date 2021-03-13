package tw.waterball.judgegirl.springboot.student.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class IdNotFoundException extends RuntimeException{
    public IdNotFoundException() {
        super();
    }

    public IdNotFoundException(String message) {
        super(message);
    }

    public IdNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdNotFoundException(Throwable cause) {
        super(cause);
    }

    protected IdNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
