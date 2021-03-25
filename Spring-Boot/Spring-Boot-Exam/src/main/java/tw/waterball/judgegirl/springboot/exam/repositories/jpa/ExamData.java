package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.Exam;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

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
    private String description;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<QuestionData> questions = new ArrayList<>();

    public Exam toEntity() {
        return new Exam(id, name, startTime, endTime, description, questions.stream().map(QuestionData::toEntity).collect(Collectors.toList()));
    }

    public static ExamData toData(Exam exam) {
        return ExamData.builder()
                .id(exam.getId())
                .name(exam.getName())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .description(exam.getDescription())
                .questions(mapToList(exam.getQuestions(),QuestionData::toData))
                .build();
    }
}
