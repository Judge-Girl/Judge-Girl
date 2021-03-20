package tw.waterball.judgegirl.springboot.student.controllers;

/**
 * @author - wally55077@gmail.com
 */
public class DuplicateGroupTitleException extends RuntimeException {

    public DuplicateGroupTitleException(){
        super("Group title can not be duplicate.");
    }
}
