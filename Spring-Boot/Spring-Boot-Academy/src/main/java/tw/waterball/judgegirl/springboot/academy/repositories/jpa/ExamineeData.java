package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.primitives.exam.Examinee;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "examinees")
@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
public class ExamineeData {
    @EmbeddedId
    private Id id;

    public ExamineeData(int examId, int studentId) {
        this(new Id(examId, studentId));
    }

    public ExamineeData(Id id) {
        this.id = id;
    }

    public Examinee toEntity() {
        return new Examinee(new Examinee.Id(id.examId, id.studentId));
    }

    public static ExamineeData toData(Examinee examinee) {
        return new ExamineeData(new Id(examinee.getId()));
    }

    public int getExamId() {
        return id.examId;
    }

    public int getStudentId() {
        return id.studentId;
    }

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private int examId;
        private int studentId;

        public Id(Examinee.Id id) {
            examId = id.getExamId();
            studentId = id.getStudentId();
        }
    }
}
