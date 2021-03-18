package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.Exam;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ExamData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String name;
    private Date startTime;
    private Date endTime;

    public Exam toEntity() {
        return new Exam(id, name, startTime, endTime);
    }

    public static ExamData toData(Exam exam) {
        return ExamData.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .build();
    }
}
