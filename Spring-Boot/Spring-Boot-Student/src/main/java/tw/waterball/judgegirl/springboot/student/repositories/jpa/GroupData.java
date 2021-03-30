package tw.waterball.judgegirl.springboot.student.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author - wally55077@gmail.com
 */
@Table(name = "groups")
@Setter
@Getter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class GroupData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToMany(cascade = CascadeType.MERGE)
    private Set<StudentData> students = new HashSet<>();

    public static GroupData toData(Group group) {
        GroupData groupData = GroupData.builder()
                .id(group.getId())
                .name(group.getName())
                .students(new HashSet<>())
                .build();
        group.getStudents().forEach(groupData::addStudentData);
        return groupData;
    }

    public static Group toEntity(GroupData groupData) {
        Group group = Group.builder()
                .id(groupData.getId())
                .name(groupData.getName())
                .students(new HashSet<>())
                .build();
        groupData.getStudents()
                .stream()
                .map(StudentData::toEntity)
                .forEach(group::addStudent);
        return group;
    }

    private void addStudentData(Student student) {
        StudentData studentData = StudentData.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .password(student.getPassword())
                .admin(student.isAdmin())
                .groups(new HashSet<>())
                .build();
        students.add(studentData);
    }

}
