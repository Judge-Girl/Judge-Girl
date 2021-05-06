package tw.waterball.judgegirl.studentapi.clients.view;

import lombok.*;
import tw.waterball.judgegirl.primitives.exam.Group;

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

    public static GroupView toViewModel(Group group) {
        return new GroupView(group.getId(), group.getName());
    }
}
