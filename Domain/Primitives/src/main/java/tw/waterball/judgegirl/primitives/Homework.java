package tw.waterball.judgegirl.primitives;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tw.waterball.judgegirl.commons.utils.validations.ValidationUtils;

import javax.validation.constraints.NotBlank;
import java.util.List;

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

    private List<Integer> problemIds;

    public Homework(String name, List<Integer> problemIds) {
        this.name = name;
        this.problemIds = problemIds;
    }

    public void validate() {
        ValidationUtils.validate(this);
    }

    public void addProblemIds(List<Integer> problemIds) {
        this.problemIds.addAll(problemIds);
    }


    public boolean containsProblemId(Integer problemId) {
        return this.problemIds.contains(problemId);
    }

    public void deleteProblemIds(List<Integer> problemIds) {
        this.problemIds.removeAll(problemIds);
    }

}
