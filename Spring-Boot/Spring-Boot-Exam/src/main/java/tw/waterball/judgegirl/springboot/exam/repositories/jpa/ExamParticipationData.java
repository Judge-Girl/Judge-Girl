package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.ExamParticipation;

import javax.persistence.*;

@Table(name = "exam_participations")
@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class ExamParticipationData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer examId;
    private Integer studentId;
    private Integer score = null;
    private Boolean absent = null;

    public ExamParticipation toEntity() {
        return new ExamParticipation(id, examId, studentId, score, absent);
    }

    public static ExamParticipationData toData(ExamParticipation examParticipation) {
        return ExamParticipationData.builder()
                .id(examParticipation.getId())
                .examId(examParticipation.getExamId())
                .studentId(examParticipation.getStudentId())
                .score(examParticipation.getScore())
                .absent(examParticipation.getAbsent())
                .build();
    }
}
