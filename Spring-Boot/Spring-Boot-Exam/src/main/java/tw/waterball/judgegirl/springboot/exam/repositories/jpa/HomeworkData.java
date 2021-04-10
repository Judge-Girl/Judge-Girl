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

    private String problemIds;

    public HomeworkData(String name, String problemIds) {
        this.name = name;
        this.problemIds = problemIds;
    }

    public static HomeworkData toData(Homework homework) {
        return new HomeworkData(homework.getName(), homework.getProblemIds());
    }

    public static Homework toEntity(HomeworkData homeworkData) {
        return new Homework(homeworkData.getId(), homeworkData.getName(), homeworkData.getProblemIds());
    }

}
