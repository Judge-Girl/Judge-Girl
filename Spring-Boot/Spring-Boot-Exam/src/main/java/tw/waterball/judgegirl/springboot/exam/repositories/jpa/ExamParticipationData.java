package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.ExamParticipation;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "exam_participations")
@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ExamParticipationData {
    @EmbeddedId
    private Id id;
    private Integer score = null;
    private Boolean absent = null;

    public ExamParticipationData(int examId, int studentId) {
        this(new Id(examId, studentId));
    }

    public ExamParticipationData(Id id) {
        this.id = id;
    }

    public ExamParticipation toEntity() {
        return new ExamParticipation(new ExamParticipation.Id(id.examId, id.studentId),
                score, absent);
    }

    public static ExamParticipationData toData(ExamParticipation examParticipation) {
        return ExamParticipationData.builder()
                .id(new Id(examParticipation.getId()))
                .score(examParticipation.getScore())
                .absent(examParticipation.getAbsent())
                .build();
    }

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private int examId;
        private int studentId;

        public Id(ExamParticipation.Id id) {
            examId = id.getExamId();
            studentId = id.getStudentId();
        }
    }

    public int getExamId() {
        return id.examId;
    }

    public int getStudentId() {
        return id.studentId;
    }
}
