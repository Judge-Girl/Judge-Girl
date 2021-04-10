package tw.waterball.judgegirl.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;

import javax.validation.constraints.NotBlank;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Homework {

    private Integer id;

    @NotBlank
    private String name;

    private String problemIds;

    public Homework(String name, String problemIds) {
        this.name = name;
        this.problemIds = problemIds;
    }

    public void validate() {
        JSR380Utils.validate(this);
    }

}
