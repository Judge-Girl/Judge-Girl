package tw.waterball.judgegirl.springboot.student.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;

import javax.persistence.*;
import java.util.*;

/**
 * @author - wally55077@gmail.com
 */
@Table(name = "student_groups" /*escape from keywords*/)
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
    @JoinTable(name = "membership",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "student_id"))
    private Set<StudentData> students = new HashSet<>();

    public GroupData(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public static GroupData toData(Group group) {
        GroupData groupData = new GroupData(group.getId(), group.getName());
        groupData.addStudents(group.getStudents());
        return groupData;
    }

    private void addStudents(Collection<Student> students) {
        List<StudentData> studentDataList = new ArrayList<>(students.size());
        for (Student student : students) {
            StudentData studentData = StudentData.builder()
                    .id(student.getId())
                    .name(student.getName())
                    .email(student.getEmail())
                    .password(student.getPassword())
                    .admin(student.isAdmin())
                    .groups(new HashSet<>())
                    .build();
            studentDataList.add(studentData);
            studentData.getGroups().add(this);
        }
        this.students.addAll(studentDataList);
    }

    public Group toEntity() {
        Group group = new Group(id, name);
        students.stream()
                .map(StudentData::toEntity)
                .forEach(group::addStudent);
        return group;
    }

}
