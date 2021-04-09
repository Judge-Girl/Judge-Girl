package tw.waterball.judgegirl.entities.exam;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;

/**
 * A record that a student achieves in a question.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
@Getter
@AllArgsConstructor
public class Record implements Comparable<Record> {
    private final Question.Id questionId;
    private final int studentId;
    private final JudgeStatus status;
    private final long maximumRuntime;
    private final long maximumMemoryUsage;
    private final int score;

    @Override
    public int compareTo(@NotNull Record record) {
        if (score != record.getScore()) {
            return score - record.getScore();
        }
        if (status.getOrder() != record.getStatus().getOrder()) {
            return status.getOrder() - record.getStatus().getOrder();
        }
        return (maximumRuntime + maximumMemoryUsage) >
                (record.getMaximumRuntime() + record.getMaximumMemoryUsage()) ?
                -1 : 1;
    }
}
