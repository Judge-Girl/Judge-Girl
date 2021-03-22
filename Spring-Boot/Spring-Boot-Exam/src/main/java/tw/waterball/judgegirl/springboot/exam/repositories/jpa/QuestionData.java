package tw.waterball.judgegirl.springboot.exam.repositories.jpa;


import lombok.*;
import tw.waterball.judgegirl.entities.Question;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class QuestionData {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    private Integer examId = null;
    private Integer problemId = null;
    private Integer quota = null;
    private Integer score = null;

    public Question toEntity() {
        return new Question(id, examId, problemId, quota, score);
    }

    public static QuestionData toData(Question question) {
        return QuestionData.builder()
                .id(question.getId())
                .examId(question.getExamId())
                .problemId(question.getProblemId())
                .quota(question.getQuota())
                .score(question.getScore())
                .build();
    }

}
