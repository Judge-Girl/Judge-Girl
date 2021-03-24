package tw.waterball.judgegirl.springboot.exam.view;

import lombok.*;
import tw.waterball.judgegirl.entities.Question;

@EqualsAndHashCode
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionOverview {
    private int id;
    private int examId;
    private int problemId;
    private int quota;
    private int score;
    private int questionOrder;

    public static QuestionOverview toViewModel(Question question) {
        return QuestionOverview.builder()
                .id(question.getId())
                .examId(question.getExamId())
                .problemId(question.getProblemId())
                .quota(question.getQuota())
                .score(question.getScore())
                .questionOrder(question.getQuestionOrder())
                .build();
    }
}
