package tw.waterball.judgegirl.springboot.academy.view;

import lombok.*;
import tw.waterball.judgegirl.commons.utils.StreamUtils;
import tw.waterball.judgegirl.commons.utils.StringUtils;
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

    @Singular
    public List<QuestionItem> questions;
    private List<String> whitelist;
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
        // If the problem is notFound the value will be null
        public Boolean archived;
        public boolean notFound;

        public static QuestionItem toViewModel(Problem problem, Question question) {
            QuestionItem questionItem = toViewModel(question);
            questionItem.problemTitle = problem.getTitle();
            questionItem.archived = problem.isArchived();
            questionItem.notFound = false;
            return questionItem;
        }

        public static QuestionItem toViewModel(Question question) {
            return QuestionItem.builder()
                    .examId(question.getExamId())
                    .problemId(question.getProblemId())
                    .quota(question.getQuota())
                    .maxScore(question.getScore())
                    .notFound(true)
                    .questionOrder(question.getQuestionOrder()).build();
        }

        public String getProblemTitle() {
            return problemTitle;
        }

        public Boolean getArchived() {
            return archived;
        }
    }

}
