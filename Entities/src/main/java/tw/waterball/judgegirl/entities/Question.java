package tw.waterball.judgegirl.entities;

import lombok.*;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Question {

    private QuestionId id = null;

    @NotNull
    private int quota;

    @NotNull
    private int score;

    @NotNull
    private int questionOrder;

    public Question(int examId, int problemId, int quota, int score, int questionOrder) {
        this.id = new QuestionId(examId, problemId);
        this.quota = quota;
        this.score = score;
        this.questionOrder = questionOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionId {
        private Integer examId;
        private Integer problemId;
    }
}
