package tw.waterball.judgegirl.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ExamParticipation {

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
