package tw.waterball.judgegirl.springboot.student.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.Student;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Builder
@Getter @Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class StudentData {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String name;
    private String email;
    private String password;

    public Student toEntity() {
        return new Student(id, name, email, password);
    }

    public static StudentData toData(Student student) {
        return StudentData.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .password(student.getPassword())
                .build();
    }

}
