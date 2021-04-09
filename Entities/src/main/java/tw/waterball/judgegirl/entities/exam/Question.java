package tw.waterball.judgegirl.entities.exam;

import lombok.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Question {
    public static final int NO_QUOTA = Integer.MAX_VALUE;
    private Id id = null;

    @Positive
    private int quota = NO_QUOTA;

    @PositiveOrZero
    private int score;

    @PositiveOrZero
    private int questionOrder;
    
    public Question(int examId, int problemId, int quota, int score, int questionOrder) {
        this.id = new Id(examId, problemId);
        this.quota = quota;
        this.score = score;
        this.questionOrder = questionOrder;
    }

    public boolean hasSubmissionQuota() {
        return quota != NO_QUOTA;
    }

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id {
        private int examId;
        private int problemId;
    }

    public int getExamId() {
        return id.getExamId();
    }

    public int getProblemId() {
        return id.getProblemId();
    }
}
