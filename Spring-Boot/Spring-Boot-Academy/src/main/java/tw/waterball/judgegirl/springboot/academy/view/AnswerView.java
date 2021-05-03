package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.exam.Answer;

import java.util.Date;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerView {
    public int number;
    public int examId;
    public int problemId;
    public int studentId;
    public String submissionId;
    private Date answerTime;

    public static AnswerView toViewModel(Answer answer) {
        return new AnswerView(answer.getNumber(), answer.getExamId(), answer.getProblemId(),
                answer.getStudentId(), answer.getSubmissionId(), answer.getAnswerTime());
    }
}
