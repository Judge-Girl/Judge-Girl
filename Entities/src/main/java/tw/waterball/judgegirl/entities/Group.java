package tw.waterball.judgegirl.entities;

import lombok.*;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
        student.getGroups().add(this);
    }

    public void addStudents(Collection<Student> students) {
        students.forEach(this::addStudent);
    }

    public void deleteStudent(Student student) {
        students.remove(student);
        student.getGroups().remove(this);
    }

    public void deleteStudentByIds(String... ids) throws NotFoundException {
        Set<Integer> studentIds = Arrays.stream(ids)
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
        students.stream()
                .filter(student -> studentIds.contains(student.id))
                .collect(Collectors.toList())
                .forEach(this::deleteStudent);
    }
}
