package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tw.waterball.judgegirl.entities.Homework;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - wally55077@gmail.com
 */
@Setter
@Getter
@Entity
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
        String problemIds = homework.getProblemIds()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return new HomeworkData(homework.getName(), problemIds);
    }

    public static Homework toEntity(HomeworkData homeworkData) {
        List<Integer> problemIds = Arrays
                .stream(homeworkData.getProblemIds().split(","))
                .filter(problemId -> !problemId.isBlank())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
        return new Homework(homeworkData.getId(), homeworkData.getName(), problemIds);
    }

}
