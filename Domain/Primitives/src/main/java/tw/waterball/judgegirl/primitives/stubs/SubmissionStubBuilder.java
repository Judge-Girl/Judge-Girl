package tw.waterball.judgegirl.primitives.stubs;

import lombok.RequiredArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.verdict.Judge;
import tw.waterball.judgegirl.primitives.submission.verdict.ProgramProfile;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.generate;
import static tw.waterball.judgegirl.primitives.problem.JudgeStatus.*;

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
@RequiredArgsConstructor
public class SubmissionStubBuilder extends Submission {
    public static final int DONT_CARE = 18729;
    private static final String DONT_CARE_STRING = "DontCare";
    private final String submissionId;
    private final Bag bag = new Bag();
    private VerdictStubBuilder verdictBuilder;

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

    public SubmissionStubBuilder CE(int maxGrade) {
        lazyInitializeVerdictIfNull();
        verdictBuilder.CE(maxGrade);
        return this;
    }

    public SubmissionStubBuilder bag(String key, String message) {
        bag.put(key, message);
        return this;
    }

    public Submission build() {
        return build(DONT_CARE, DONT_CARE, DONT_CARE_STRING);
    }

    public Submission build(int studentId) {
        return build(studentId, DONT_CARE, DONT_CARE_STRING);
    }

    public Submission build(int studentId, int problemId, String languageEnvName) {
        Submission submission = new Submission(submissionId, studentId, problemId, languageEnvName, "s", new Date());
        submission.setBag(bag);
        if (verdictBuilder != null) {
            submission.setVerdict(verdictBuilder.build());
        }
        return submission;
    }

    @Override
    public String getId() {
        return submissionId;
    }

    @Override
    public Optional<Verdict> mayHaveVerdict() {
        return verdictBuilder == null ? Optional.empty() : Optional.of(verdictBuilder.build());
    }

    private SubmissionStubBuilder judge(JudgeStatus status, long runtime, long memoryUsage, int grade) {
        lazyInitializeVerdictIfNull();
        verdictBuilder.judge(status, runtime, memoryUsage, grade);
        return this;
    }

    private void lazyInitializeVerdictIfNull() {
        if (verdictBuilder == null) {
            verdictBuilder = VerdictStubBuilder.verdict();
        }
    }

    /**
     * @param problem    The problem the submission submits to.
     *                   The problem's testcases are used to create the corresponding judges.
     * @param studentId  The id of the student who submits the submission.
     * @param howManyACs Number of AC judges that will be given in the submission.
     * @param gradePerAc The grade of every AC judge.
     * @return A randomized and judged submission.
     */
    public static Submission randomizedSubmission(Problem problem, int studentId, int howManyACs) {
        Random random = new Random(currentTimeMillis());
        List<JudgeStatus> statuses = generate(howManyACs, AC);
        statuses.addAll(generate(problem.numOfTestcases() - howManyACs,
                i -> NORMAL_STATUSES_NO_CE[random.nextInt(NORMAL_STATUSES_NO_CE.length)]));

        List<Judge> judges = generate(problem.numOfTestcases(),
                i -> new Judge(problem.getTestcases().get(i),
                        statuses.get(i), new ProgramProfile(10, 10,
                        statuses.get(i) == RE ? "Error" : ""),
                        statuses.get(i) == AC ? problem.getTestcase(i).getGrade() : 0));

        Submission submission = new Submission(randomUUID().toString(),
                studentId, problem.getId(), Language.C.toString(), "fileId");
        submission.setVerdict(new Verdict(judges));
        return submission;
    }
}
