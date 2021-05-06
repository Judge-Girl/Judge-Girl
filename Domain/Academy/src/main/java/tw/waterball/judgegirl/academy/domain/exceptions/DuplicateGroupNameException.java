package tw.waterball.judgegirl.academy.domain.exceptions;

/**
 * @author - wally55077@gmail.com
 */
public class DuplicateGroupNameException extends IllegalStateException {

    public DuplicateGroupNameException() {
        super("Group name can not be duplicate.");
    }
}
