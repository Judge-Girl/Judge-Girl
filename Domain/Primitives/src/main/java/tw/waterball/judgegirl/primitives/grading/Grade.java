package tw.waterball.judgegirl.primitives.grading;

import lombok.EqualsAndHashCode;

import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.sum;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@EqualsAndHashCode
public class Grade {
    private final int value;
    private final int maxGrade;

    public Grade(int value, int maxGrade) {
        this.value = value;
        this.maxGrade = maxGrade;
        if (value > maxGrade) {
            throw new IllegalStateException("The grade's value should not exceed the maxGrade.");
        }
    }

    public Grade(List<? extends Grading> gradings) {
        this.value = sum(gradings, Grading::getGrade);
        this.maxGrade = sum(gradings, Grading::getMaxGrade);
    }

    public boolean isFull() {
        return value == maxGrade;
    }

    public int value() {
        return value;
    }

    public int max() {
        return maxGrade;
    }

    @Override
    public String toString() {
        return String.format("%d/%d", value, maxGrade);
    }
}
