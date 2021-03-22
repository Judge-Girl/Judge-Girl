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
public class Question {

    private Integer id = null;

    @NotNull
    private int examId;

    @NotNull
    private int problemId;

    @NotNull
    private int quota;

    @NotNull
    private int score;

    public Question(int examId, int problemId, int quota, int score){
        this.examId=examId;
        this.problemId=problemId;
        this.quota=quota;
        this.score=score;
    }
}
