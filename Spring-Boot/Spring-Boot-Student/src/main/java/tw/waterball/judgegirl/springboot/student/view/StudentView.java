package tw.waterball.judgegirl.springboot.student.view;

import lombok.*;
import tw.waterball.judgegirl.entities.Student;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@EqualsAndHashCode
@Builder
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentView {
    private Integer id;
    private String name;
    private String email;

    public static StudentView toViewModel(Student student) {
        return StudentView.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .build();
    }
}
