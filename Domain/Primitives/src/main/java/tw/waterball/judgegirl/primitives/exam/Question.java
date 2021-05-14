package tw.waterball.judgegirl.primitives.exam;

import lombok.*;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static tw.waterball.judgegirl.commons.utils.JSR380Utils.validate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Question {
    public static final int NO_QUOTA_LIMITATION = Integer.MAX_VALUE;
    private Id id;

    @Positive
    private int quota = NO_QUOTA_LIMITATION;

    @PositiveOrZero
    private int score;

    @PositiveOrZero
    private int questionOrder;

    public Question(int examId, int problemId, int quota, int score, int questionOrder) {
        this.id = new Id(examId, problemId);
        this.quota = quota;
        this.score = score;
        this.questionOrder = questionOrder;
        validate(this);
    }

    public boolean hasSubmissionQuotaLimitation() {
        return quota != NO_QUOTA_LIMITATION;
    }

    public int getExamId() {
        return id.getExamId();
    }

    public int getProblemId() {
        return id.getProblemId();
    }

    @Data
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id {
        private int examId;
        private int problemId;
    }
}
