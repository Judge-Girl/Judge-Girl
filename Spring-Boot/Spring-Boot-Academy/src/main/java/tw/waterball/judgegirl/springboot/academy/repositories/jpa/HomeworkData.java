package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tw.waterball.judgegirl.primitives.Homework;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.join;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@Entity(name = "homework")
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    // split by commas
    private String problemIds;

    public HomeworkData(String name, String problemIds) {
        this.name = name;
        this.problemIds = problemIds;
    }

    public static HomeworkData toData(Homework homework) {
        String problemIds = join(homework.getProblemIds(), ",");
        return new HomeworkData(homework.getId(), homework.getName(), problemIds);
    }

    public static Homework toEntity(HomeworkData homeworkData) {
        List<Integer> problemIds;
        if (homeworkData.getProblemIds().isBlank()) {
            problemIds = Collections.emptyList();
        } else {
            problemIds = Arrays.stream(homeworkData.getProblemIds().split("\\s*,\\s*"))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        }
        return new Homework(homeworkData.getId(), homeworkData.getName(), problemIds);
    }

}
