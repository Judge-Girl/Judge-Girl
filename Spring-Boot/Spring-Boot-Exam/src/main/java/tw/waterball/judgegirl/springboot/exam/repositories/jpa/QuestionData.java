package tw.waterball.judgegirl.springboot.exam.repositories.jpa;


import lombok.*;
import tw.waterball.judgegirl.entities.Question;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;

@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class QuestionData {

    @EmbeddedId
    private Id id;

    private int quota;
    private int score;
    private int questionOrder;

    public Question toEntity() {
        return new Question(id.examId, id.problemId, quota, score, questionOrder);
    }

    public static QuestionData toData(Question question) {
        return QuestionData.builder()
                .id(new Id(question.getId().getExamId(), question.getId().getProblemId()))
                .quota(question.getQuota())
                .score(question.getScore())
                .questionOrder(question.getQuestionOrder())
                .build();
    }

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private int examId;
        private int problemId;
    }
}
