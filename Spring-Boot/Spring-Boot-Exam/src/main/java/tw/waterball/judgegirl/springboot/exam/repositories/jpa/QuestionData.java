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

    private int examId;
    private int problemId;
    private int quota;
    private int score;
    private int questionOrder;

    public Question toEntity() {
        return new Question(id, examId, problemId, quota, score, questionOrder);
    }

    public static QuestionData toData(Question question) {
        return QuestionData.builder()
                .id(question.getId())
                .examId(question.getExamId())
                .problemId(question.getProblemId())
                .quota(question.getQuota())
                .score(question.getScore())
                .questionOrder(question.getQuestionOrder())
                .build();
    }

}
