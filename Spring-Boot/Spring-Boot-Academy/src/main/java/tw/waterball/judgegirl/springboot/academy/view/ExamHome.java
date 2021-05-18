package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.exam.Record;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.problem.Problem;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExamHome {
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
        public int remainingQuota;
        public int maxScore;
        public int questionOrder;
        public int yourScore;
        public BestRecord bestRecord;
        public String problemTitle;

        public static QuestionItem toViewModel(Question question, Problem problem,
                                               int remainingQuota, int yourScore,
                                               @Nullable Record bestRecord) {
            var builder = QuestionItem.builder()
                    .examId(question.getExamId())
                    .problemId(problem.getId())
                    .quota(question.getQuota())
                    .remainingQuota(remainingQuota)
                    .yourScore(yourScore)
                    .maxScore(question.getScore())
                    .questionOrder(question.getQuestionOrder())
                    .problemTitle(problem.getTitle());
            if (bestRecord != null) {
                builder = builder.bestRecord(new BestRecord(bestRecord));
            }
            return builder.build();
        }

    }

    @Builder
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BestRecord {
        public int score;
        public JudgeStatus status;
        public long maximumRuntime;
        public long maximumMemoryUsage;
        public Date submissionTime;

        public BestRecord(Record record) {
            this(record.getGrade(), record.getStatus(), record.getMaximumRuntime(),
                    record.getMaximumMemoryUsage(), record.getSubmissionTime());
        }
    }
}
