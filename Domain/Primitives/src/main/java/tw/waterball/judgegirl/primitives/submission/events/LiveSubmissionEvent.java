package tw.waterball.judgegirl.primitives.submission.events;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.Submission;

import java.util.Date;

import static java.util.stream.Collectors.joining;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class LiveSubmissionEvent extends Event {
    private final int problemId;
    private final String languageEnvName;
    private final int studentId;
    private final String submissionId;
    private final Date submissionTime;
    private final Bag submissionBag;

    public LiveSubmissionEvent(int problemId, String languageEnvName, int studentId, String submissionId, Date submissionTime, Bag submissionBag) {
        super(LiveSubmissionEvent.class.getSimpleName());
        this.problemId = problemId;
        this.languageEnvName = languageEnvName;
        this.studentId = studentId;
        this.submissionId = submissionId;
        this.submissionTime = submissionTime;
        this.submissionBag = submissionBag;
    }

    public static LiveSubmissionEvent liveSubmission(Submission submission) {
        return new LiveSubmissionEvent(submission.getProblemId(),
                submission.getLanguageEnvName(),
                submission.getStudentId(),
                submission.getId(),
                submission.getSubmissionTime(),
                submission.getBag());
    }

    @Override
    public String toString() {
        return String.format("problemId=%d languageEnvName=%s studentId=%d submissionId=%s submissionTime=%d, with bag: %s",
                problemId, languageEnvName, studentId, submissionId, submissionTime.getTime(),
                submissionBag.entrySet().stream()
                        .map(entry -> String.format("%s=\"%s\"", entry.getKey(), entry.getValue()))
                        .collect(joining(" ")));
    }
}
