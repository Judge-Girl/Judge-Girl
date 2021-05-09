package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.problem.Problem;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamOverview {
    public Integer id;
    public String name;
    public Date startTime;
    public Date endTime;
    public String description;
    public List<QuestionItem> questions;
    public int totalScore;

    public Optional<QuestionItem> getQuestionById(Question.Id questionId) {
        return questions.stream()
                .filter(q -> q.getExamId() == questionId.getExamId() && q.getProblemId() == questionId.getProblemId())
                .findFirst();
    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QuestionItem {
        public int examId;
        public int problemId;
        public int quota;
        public int maxScore;
        public int questionOrder;
        public String problemTitle;

        public static QuestionItem toViewModel(Question question, Problem problem) {
            return QuestionItem.builder()
                    .examId(question.getExamId())
                    .problemId(problem.getId())
                    .quota(question.getQuota())
                    .maxScore(question.getScore())
                    .questionOrder(question.getQuestionOrder())
                    .problemTitle(problem.getTitle()).build();
        }
    }

}
