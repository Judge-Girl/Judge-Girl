package tw.waterball.judgegirl.springboot.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ExamParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id = null;

    @NotNull
    private int examId;

    @NotNull
    private int studentId;

    private Integer score = null;

    private Boolean absent = null;

    public ExamParticipation(int examId, int studentId) {
        this.examId = examId;
        this.studentId = studentId;
    }

}
