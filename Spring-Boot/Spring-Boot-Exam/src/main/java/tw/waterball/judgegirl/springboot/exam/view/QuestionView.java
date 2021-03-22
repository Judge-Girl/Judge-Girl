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
    private Integer id;
    private Integer examId;
    private Integer problemId;
    private Integer quota;
    private Integer score;

    public static QuestionView toViewModel(Question question) {
        return QuestionView.builder()
                .id(question.getId())
                .examId(question.getExamId())
                .problemId(question.getProblemId())
                .quota(question.getQuota())
                .score(question.getScore())
                .build();
    }
}
