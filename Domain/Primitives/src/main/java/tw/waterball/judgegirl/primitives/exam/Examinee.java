package tw.waterball.judgegirl.primitives.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class Examinee {
    private final Id id;

    public Examinee(int examId, int studentId) {
        id = new Id(examId, studentId);
    }

    public int getExamId() {
        return id.getExamId();
    }

    public int getStudentId() {
        return id.getStudentId();
    }

    @Getter
    @AllArgsConstructor
    public static class Id implements Serializable {
        private final int examId;
        private final int studentId;
    }
}
