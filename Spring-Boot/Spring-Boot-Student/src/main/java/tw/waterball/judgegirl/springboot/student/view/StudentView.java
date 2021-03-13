package tw.waterball.judgegirl.springboot.student.view;

import lombok.EqualsAndHashCode;
import lombok.Value;
import tw.waterball.judgegirl.entities.Student;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Value
public class StudentView {

    public static StudentView toViewModel(Student student) {
        return null;
    }
}
