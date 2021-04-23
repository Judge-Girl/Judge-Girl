package tw.waterball.judgegirl.springboot.exam.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.entities.exam.Record;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.problem.Problem;

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
        public BestRecord bestRecord;
        public String problemTitle;

        public static QuestionItem toViewModel(Question question, Problem problem,
                                               int remainingQuota,
                                               @Nullable Record bestRecord) {
            var builder = QuestionItem.builder()
                    .examId(question.getExamId())
                    .problemId(question.getId().getProblemId())
                    .quota(question.getQuota())
                    .remainingQuota(remainingQuota)
                    .maxScore(problem.getTotalGrade())
                    .questionOrder(question.getQuestionOrder())
                    .problemTitle(problem.getTitle());
            if (bestRecord != null) {
                builder = builder.bestRecord(new BestRecord(bestRecord));
            }
            return builder.build();
        }

        public int getYourScore() {
            return bestRecord == null ? 0 : bestRecord.getScore();
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
            this(record.getScore(), record.getStatus(), record.getMaximumRuntime(),
                    record.getMaximumMemoryUsage(), record.getSubmissionTime());
        }
    }
}
