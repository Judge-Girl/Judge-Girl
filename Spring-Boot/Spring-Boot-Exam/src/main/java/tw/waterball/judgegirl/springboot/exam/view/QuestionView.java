package tw.waterball.judgegirl.springboot.exam.view;

import lombok.*;
import tw.waterball.judgegirl.entities.exam.Question;

@EqualsAndHashCode
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionView {
    public int examId;
    public int problemId;
    public int quota;
    public int score;
    public int questionOrder;

    public static QuestionView toViewModel(Question question) {
        return QuestionView.builder()
                .examId(question.getId().getExamId())
                .problemId(question.getId().getProblemId())
                .quota(question.getQuota())
                .score(question.getScore())
                .questionOrder(question.getQuestionOrder())
                .build();
    }
}
