package tw.waterball.judgegirl.entities;

import lombok.*;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;

import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    private Integer id;

    @NotBlank
    private String name;

    private Set<Student> students = new HashSet<>();

    public Group(String name) {
        this.name = name;
    }

    public Group(Integer id, String name) {
        this(name);
        this.id = id;
    }

    public void validate() {
        JSR380Utils.validate(this);
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public void deleteStudent(Student student) {
        students.remove(student);
    }
}
