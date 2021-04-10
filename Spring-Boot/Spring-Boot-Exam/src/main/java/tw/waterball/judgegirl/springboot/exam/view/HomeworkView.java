package tw.waterball.judgegirl.springboot.exam.view;

import lombok.*;
import tw.waterball.judgegirl.entities.Homework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<String> problemIds = new ArrayList<>();

    public static HomeworkView toViewModel(Homework homework) {
        List<String> problemIds = Arrays.stream(homework.getProblemIds().split(","))
                .collect(Collectors.toList());
        return new HomeworkView(homework.getId(), homework.getName(), problemIds);
    }

}
