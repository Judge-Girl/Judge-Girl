package tw.waterball.judgegirl.examservice.repositories;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@Getter
public
class ExamFilter {
    Integer studentId;
    @Builder.Default
    Status status = Status.all;
    @Builder.Default
    int skip = 0;
    @Builder.Default
    int size = 50;

    public enum Status {
        all, past, current, upcoming
    }

    public static ExamFilter.ExamFilterBuilder studentId(int studentId) {
        return ExamFilter.builder().studentId(studentId);
    }

    public Optional<Integer> getStudentId() {
        return ofNullable(studentId);
    }
}
