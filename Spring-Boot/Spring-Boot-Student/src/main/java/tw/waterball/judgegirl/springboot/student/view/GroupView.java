package tw.waterball.judgegirl.springboot.student.view;

import lombok.*;
import tw.waterball.judgegirl.entities.Group;

/**
 * @author - wally55077@gmail.com
 */
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class GroupView {

    public int id;

    public String name;

    public GroupView(String name) {
        this.name = name;
    }

    public static GroupView toViewModel(Group group) {
        return GroupView.builder()
                .id(group.getId())
                .name(group.getName())
                .build();
    }
}
