package tw.waterball.judgegirl.primitives.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Getter
@AllArgsConstructor
public class Answer {
    private final Answer.Id id;
    private final String submissionId;
    private final Date answerTime;


    @Getter
    @Setter
    @AllArgsConstructor
    public static class Id {
        private Integer number;
        private final Question.Id questionId;
        private final int studentId;

        public Id(Question.Id questionId, int studentId) {
            this.questionId = questionId;
            this.studentId = studentId;
        }
    }

    public int getNumber() {
        return getId().getNumber();
    }

    public Question.Id getQuestionId() {
        return getId().getQuestionId();
    }

    public int getExamId() {
        return getId().getQuestionId().getExamId();
    }

    public int getProblemId() {
        return getId().getQuestionId().getProblemId();
    }

    public int getStudentId() {
        return getId().getStudentId();
    }

}
