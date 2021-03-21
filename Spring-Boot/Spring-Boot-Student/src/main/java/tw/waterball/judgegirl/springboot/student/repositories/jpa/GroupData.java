package tw.waterball.judgegirl.springboot.student.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.Group;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class GroupData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String name;

    public static GroupData toData(Group group) {
        return GroupData.builder()
                .name(group.getName())
                .build();
    }

}
