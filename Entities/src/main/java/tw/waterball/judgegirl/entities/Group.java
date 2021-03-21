package tw.waterball.judgegirl.entities;

import lombok.*;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;

import javax.validation.constraints.NotBlank;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    private Integer id;

    @NotBlank
    private String name;

    public Group(String name) {
        this.name = name;
    }

    public void validate() {
        JSR380Utils.validate(this);
    }

}
