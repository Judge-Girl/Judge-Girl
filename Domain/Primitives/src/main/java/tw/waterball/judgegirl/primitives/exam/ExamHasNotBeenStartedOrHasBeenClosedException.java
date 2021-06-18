package tw.waterball.judgegirl.primitives.exam;

import tw.waterball.judgegirl.primitives.time.Duration;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ExamHasNotBeenStartedOrHasBeenClosedException extends IllegalStateException {
    private final transient Duration duration;

    public ExamHasNotBeenStartedOrHasBeenClosedException(Exam exam) {
        this.duration = exam.getDuration();
    }

    public Duration getDuration() {
        return duration;
    }
}
