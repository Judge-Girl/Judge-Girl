package tw.waterball.judgegirl.springboot.academy.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.exam.Record;
import tw.waterball.judgegirl.primitives.grading.Grade;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Builder
@Getter
@Setter
@Entity(name = "best_records")
@AllArgsConstructor
@NoArgsConstructor
public class BestRecordData {
    @EmbeddedId
    private Id id;
    private String submissionId;
    @Enumerated(EnumType.STRING)
    private JudgeStatus status;
    private long maximumRuntime;
    private long maximumMemoryUsage;
    private int grade;
    private int maxGrade;
    private Date submissionTime;

    public Record toEntity() {
        return new Record(new Question.Id(getExamId(), getProblemId()), getStudentId(), getSubmissionId(),
                status, maximumRuntime, maximumMemoryUsage, new Grade(grade, maxGrade), submissionTime);
    }

    public static BestRecordData toData(Record value) {
        return new BestRecordData(new Id(new QuestionData.Id(value.getQuestionId()), value.getStudentId()),
                value.getSubmissionId(),
                value.getStatus(), value.getMaximumRuntime(), value.getMaximumMemoryUsage(),
                value.getGrade(), value.getMaxGrade(), value.getSubmissionTime());
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

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Id implements Serializable {
        private static final long serialVersionUID = 1L;
        private int examId;
        private int problemId;
        private int studentId;

        public Id(QuestionData.Id id, int studentId) {
            examId = id.getExamId();
            problemId = id.getProblemId();
            this.studentId = studentId;
        }
    }

}
