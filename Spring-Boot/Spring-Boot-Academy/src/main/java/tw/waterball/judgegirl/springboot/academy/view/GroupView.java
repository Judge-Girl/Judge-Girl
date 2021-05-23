package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.exam.Group;

/**
 * @author - wally55077@gmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupView {
    public int id;
    public String name;

    public static GroupView toViewModel(Group group) {
        return new GroupView(group.getId(), group.getName());
    }
}
