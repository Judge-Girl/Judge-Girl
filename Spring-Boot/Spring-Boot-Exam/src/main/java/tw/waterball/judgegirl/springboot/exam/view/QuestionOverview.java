package tw.waterball.judgegirl.springboot.exam.view;

import lombok.*;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

@EqualsAndHashCode
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuestionOverview {
    private int problemId;
    private int quota;
    private int score;
    private int questionOrder;
    private String problemTitle;

    public static QuestionOverview toViewModel(Question question, ProblemView problemView) {
        return QuestionOverview.builder()
                .problemId(question.getId().getProblemId())
                .quota(question.getQuota())
                .score(question.getScore())
                .questionOrder(question.getQuestionOrder())
                .problemTitle(problemView.getTitle())
                .build();
    }
}
