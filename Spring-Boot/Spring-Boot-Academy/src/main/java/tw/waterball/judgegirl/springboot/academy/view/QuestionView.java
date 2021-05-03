package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.exam.Question;

@Data
@Builder
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
