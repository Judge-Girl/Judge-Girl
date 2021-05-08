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
@AllArgsConstructor
@NoArgsConstructor
public class ExamineeData {
    @EmbeddedId
    private Id id;
    private Integer score;
    private Boolean absent;

    public ExamineeData(int examId, int studentId) {
        this(new Id(examId, studentId));
    }

    public ExamineeData(Id id) {
        this.id = id;
    }

    public Examinee toEntity() {
        return new Examinee(new Examinee.Id(id.examId, id.studentId),
                score, absent);
    }

    public static ExamineeData toData(Examinee examinee) {
        return ExamineeData.builder()
                .id(new Id(examinee.getId()))
                .score(examinee.getScore())
                .absent(examinee.getAbsent())
                .build();
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
