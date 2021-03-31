package tw.waterball.judgegirl.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class ExamParticipation {
    private final Id id;
    private Integer score;
    private Boolean absent;

    public ExamParticipation(int examId, int studentId) {
        id = new Id(examId, studentId);
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setAbsent(boolean absent) {
        this.absent = absent;
    }

    @Getter
    @AllArgsConstructor
    public static class Id implements Serializable {
        private final int examId;
        private final int studentId;
    }

}
