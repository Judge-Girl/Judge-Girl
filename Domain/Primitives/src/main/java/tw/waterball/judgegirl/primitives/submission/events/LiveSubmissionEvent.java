package tw.waterball.judgegirl.primitives.submission.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.Submission;

import java.util.Date;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
public class LiveSubmissionEvent {
    private final int problemId;
    private final String languageEnvName;
    private final int studentId;
    private final String submissionId;
    private final Date submissionTime;
    private final Bag submissionBag;

    public static LiveSubmissionEvent liveSubmission(Submission submission) {
        return new LiveSubmissionEvent(submission.getProblemId(),
                submission.getLanguageEnvName(),
                submission.getStudentId(),
                submission.getId(),
                submission.getSubmissionTime(),
                submission.getBag());
    }

}
