package tw.waterball.judgegirl.entities.stubs;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.submission.Judge;
import tw.waterball.judgegirl.entities.submission.ProgramProfile;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.entities.submission.Verdict;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static tw.waterball.judgegirl.entities.problem.JudgeStatus.*;

/**
 * A SubmissionStubBuilder itself is also a Submission.
 * Which is a syntax-sugar that can easily make your test case readable.
 * <pre>
 * Usage (A submission with 3 judges):
 *      submission("A").WA(10, 50)
 *          .RE()
 *          .AC(30, 50, 10)
 * </pre>
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("ALL")
@AllArgsConstructor
public class SubmissionStubBuilder extends Submission {
    private final String submissionId;
    private final List<Judge> judges = new LinkedList<>();

    public static SubmissionStubBuilder submission(String id) {
        return new SubmissionStubBuilder(id);
    }

    public SubmissionStubBuilder AC(long runtime, long memoryUsage, int grade) {
        return judge(AC, runtime, memoryUsage, grade);
    }

    public SubmissionStubBuilder SYSTEM_ERROR(long runtime, long memoryUsage) {
        return judge(SYSTEM_ERR, runtime, memoryUsage, 0);
    }

    public SubmissionStubBuilder WA(long runtime, long memoryUsage) {
        return judge(WA, runtime, memoryUsage, 0);
    }

    public SubmissionStubBuilder TLE(long runtime, long memoryUsage) {
        return judge(TLE, runtime, memoryUsage, 0);
    }

    public SubmissionStubBuilder MLE(long runtime, long memoryUsage) {
        return judge(MLE, runtime, memoryUsage, 0);
    }

    public SubmissionStubBuilder RE(long runtime, long memoryUsage) {
        return judge(RE, runtime, memoryUsage, 0);
    }

    public SubmissionStubBuilder CE() {
        judges.add(new Judge("T", CE, ProgramProfile.onlyCompileError("error"), 0));
        return this;
    }

    public Submission build() {
        Submission submission = new Submission(submissionId, 1, 1, "C", "s", new Date());
        if (!judges.isEmpty()) {
            submission.setVerdict(new Verdict(judges));
        }
        return submission;
    }

    @Override
    public String getId() {
        return submissionId;
    }

    @Override
    public Optional<Verdict> getVerdict() {
        return judges.isEmpty() ? Optional.empty() : Optional.of(new Verdict(judges));
    }

    private SubmissionStubBuilder judge(JudgeStatus status, long runtime, long memoryUsage, int grade) {
        judges.add(new Judge("T", status, new ProgramProfile(runtime, memoryUsage, ""), grade));
        return this;
    }
}
