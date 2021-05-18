package tw.waterball.judgegirl.primitives.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.primitives.grading.Grade;
import tw.waterball.judgegirl.primitives.grading.Grading;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;

import java.util.Date;

/**
 * A record that a student achieves in a question.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
@Getter
@AllArgsConstructor
public class Record implements Comparable<Record>, Grading {
    private final Question.Id questionId;
    private final int studentId;
    private final JudgeStatus status;
    private final long maximumRuntime;
    private final long maximumMemoryUsage;
    private final Grade grade;
    private final Date submissionTime;

    @Override
    public int compareTo(@NotNull Record record) {
        if (getGrade() != record.getGrade()) {
            return getGrade() - record.getGrade();
        }
        if (status.getOrder() != record.getStatus().getOrder()) {
            return status.getOrder() - record.getStatus().getOrder();
        }
        return (maximumRuntime + maximumMemoryUsage) >
                (record.getMaximumRuntime() + record.getMaximumMemoryUsage()) ?
                -1 : 1;
    }

    @Override
    public int getGrade() {
        return grade.value();
    }

    @Override
    public int getMaxGrade() {
        return grade.max();
    }
}
