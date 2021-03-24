package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.Exam;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<QuestionData> questions = new ArrayList<>();

    public Exam toEntity() {
        return new Exam(id, name, startTime, endTime, questions.stream().map(QuestionData::toEntity).collect(Collectors.toList()));
    }

    public static ExamData toData(Exam exam) {
        return ExamData.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .questions(exam.getQuestions().stream().map(QuestionData::toData).collect(Collectors.toList()))
                .build();
    }
}
