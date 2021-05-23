package tw.waterball.judgegirl.primitives.exam;

import lombok.*;
import tw.waterball.judgegirl.primitives.grading.Grading;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import static tw.waterball.judgegirl.commons.utils.ValidationUtils.validate;

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


    public Question(Id questionId, int quota, int score, int questionOrder) {
        this(questionId.examId, questionId.problemId, quota, score, questionOrder);
    }

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

    public int calculateScore(Grading grading) {
        int grade = grading.getGrade();
        int max = grading.getMaxGrade();
        return grade * getScore() / max;
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
