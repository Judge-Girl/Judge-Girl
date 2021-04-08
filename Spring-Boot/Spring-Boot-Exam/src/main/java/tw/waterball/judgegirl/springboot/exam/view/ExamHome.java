package tw.waterball.judgegirl.springboot.exam.view;

import lombok.*;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.entities.exam.Record;
import tw.waterball.judgegirl.entities.problem.Problem;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamHome {
    private Integer id;
    private String name;
    private Date startTime;
    private Date endTime;
    private String description;
    private List<QuestionItem> questions;
    private int totalScore;

    public Optional<QuestionItem> getQuestionById(Question.Id questionId) {
        return questions.stream()
                .filter(q -> q.getExamId() == questionId.getExamId() && q.getProblemId() == questionId.getProblemId())
                .findFirst();
    }

    @EqualsAndHashCode
    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionItem {
        private int examId;
        private int problemId;
        private int quota;
        private int remainingQuota;
        private int yourScore;
        private int maxScore;
        private int questionOrder;
        private String problemTitle;

        public static QuestionItem toViewModel(Question question, Problem problem,
                                               int remainingQuota,
                                               Record bestRecord) {
            return QuestionItem.builder()
                    .examId(question.getExamId())
                    .problemId(question.getId().getProblemId())
                    .quota(question.getQuota())
                    .remainingQuota(remainingQuota)
                    .yourScore(bestRecord == null ? 0 : bestRecord.getScore())
                    .maxScore(problem.getTotalGrade())
                    .questionOrder(question.getQuestionOrder())
                    .problemTitle(problem.getTitle())
                    .build();
        }
    }
}
