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

import java.util.*;

import static java.lang.System.currentTimeMillis;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
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

    public SubmissionStubBuilder CE() {
        lazyInitializeVerdictIfNull();
        verdictBuilder.CE();
        return this;
    }

    public SubmissionStubBuilder bag(String key, String message) {
        bag.put(key, message);
        return this;
    }

    public Submission build() {
        return build(DONT_CARE, DONT_CARE, DONT_CARE_STRING);
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

    public static Submission randomJudgedSubmissionFromProblem(Problem problem, int studentId, int howManyACs, int gradePerAc) {
        var judges = new ArrayList<Judge>();
        List<JudgeStatus> statuses = range(0, howManyACs).mapToObj(i -> AC).collect(toList());
        Random random = new Random(currentTimeMillis());
        JudgeStatus[] NORMAL_JUDGE_STATUSES = JudgeStatus.NORMAL_STATUSES;
        for (int i = 0; i < problem.getTestcases().size() - howManyACs; i++) {
            statuses.add(NORMAL_JUDGE_STATUSES[random.nextInt(NORMAL_JUDGE_STATUSES.length)]);
        }
        for (int i = 0; i < problem.getTestcases().size(); i++) {
            judges.add(new Judge(problem.getTestcases().get(i).getName(),
                    statuses.get(i), new ProgramProfile(10, 10,
                    statuses.get(i) == RE ? "Error" : ""),
                    statuses.get(i) == AC ? gradePerAc : 0));
        }
        Submission submission = new Submission(UUID.randomUUID().toString(), studentId, problem.getId(),
                Language.C.toString(), "fileId");
        submission.setVerdict(new Verdict(judges));
        return submission;
    }
}
