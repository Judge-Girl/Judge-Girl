package tw.waterball.judgegirl.springboot.exam.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.exam.Question;
import tw.waterball.judgegirl.entities.exam.Record;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Date;

@Builder
@Getter
@Setter
@Entity(name = "bestRecords")
@AllArgsConstructor
@NoArgsConstructor
public class BestRecordData {
    @EmbeddedId
    private Id id;
    private JudgeStatus status;
    private long maximumRuntime;
    private long maximumMemoryUsage;
    private int score;
    private Date submissionTime;


    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private int examId;
        private int problemId;
        private int studentId;

        public Id(QuestionData.Id id, int studentId) {
            examId = id.getExamId();
            problemId = id.getProblemId();
            this.studentId = studentId;
        }
    }

    public Record toEntity() {
        return new Record(new Question.Id(getExamId(), getProblemId()), getStudentId(),
                status, maximumRuntime, maximumMemoryUsage, score, submissionTime);
    }

    public static BestRecordData toData(Record value) {
        return new BestRecordData(new Id(new QuestionData.Id(value.getQuestionId()), value.getStudentId()),
                value.getStatus(), value.getMaximumRuntime(), value.getMaximumMemoryUsage(),
                value.getScore(), value.getSubmissionTime());
    }

    public int getExamId() {
        return id.examId;
    }

    public int getProblemId() {
        return id.problemId;
    }

    public int getStudentId() {
        return id.studentId;
    }


}
