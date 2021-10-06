package tw.waterball.judgegirl.springboot.academy.view;

import lombok.*;
import tw.waterball.judgegirl.primitives.Homework;

import java.util.ArrayList;
import java.util.List;

/**
 * @author - wally55077@gmail.com
 */
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkView {

    public int id;

    public String name = "";

    public List<Integer> problemIds = new ArrayList<>();

    public static HomeworkView toViewModel(Homework homework) {
        return new HomeworkView(homework.getId(), homework.getName(), homework.getProblemIds());
    }

    public boolean containProblems(List<Integer> problemIds) {
        return this.problemIds.containsAll(problemIds);
    }
}
