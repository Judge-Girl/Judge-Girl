package tw.waterball.judgegirl.springboot.exam.repositories.jpa.impl;

/**
 * @author - wally55077@gmail.com
 */
public interface CascadeGroupDeletion {

    void deleteById(int groupId);

    void deleteAll();

}
