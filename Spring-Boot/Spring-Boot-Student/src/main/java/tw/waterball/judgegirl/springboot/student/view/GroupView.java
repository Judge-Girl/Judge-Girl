package tw.waterball.judgegirl.springboot.student.view;

import lombok.*;
import tw.waterball.judgegirl.entities.Group;

import javax.validation.constraints.NotBlank;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class GroupView {

    protected Integer id;

    @NotBlank
    protected String name;

    public GroupView(@NotBlank String name) {
        this.name = name;
    }

    public static GroupView toViewModel(Group group) {
        return GroupView.builder()
                .id(group.getId())
                .name(group.getName())
                .build();
    }
}
