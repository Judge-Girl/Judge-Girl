package tw.waterball.judgegirl.primitives.exam;

import tw.waterball.judgegirl.commons.exceptions.ForbiddenAccessException;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ExamineeOnlyOperationException extends ForbiddenAccessException {
    public ExamineeOnlyOperationException() {
        super("Only the examinees can access the exam.");
    }
}
