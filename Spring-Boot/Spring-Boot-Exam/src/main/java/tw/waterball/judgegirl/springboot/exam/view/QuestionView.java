package tw.waterball.judgegirl.springboot.exam.view;

import lombok.*;
import tw.waterball.judgegirl.entities.Question;

@EqualsAndHashCode
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionView {
    private int examId;
    private int problemId;
    private int quota;
    private int score;
    private int questionOrder;

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
