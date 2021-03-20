package tw.waterball.judgegirl.springboot.student.controllers;

/**
 * @author - wally55077@gmail.com
 */
public class DuplicateGroupNameException extends RuntimeException {

    public DuplicateGroupNameException(){
        super("Group name can not be duplicate.");
    }
}
