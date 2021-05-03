package tw.waterball.judgegirl.primitives.stubs;

import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.verdict.Judge;
import tw.waterball.judgegirl.primitives.submission.verdict.ProgramProfile;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;

import java.util.LinkedList;
import java.util.List;

import static tw.waterball.judgegirl.primitives.problem.JudgeStatus.*;

/**
 * @author - wally55077@gmail.com
 */
public class VerdictStubBuilder {
    private @Nullable
    String compileError;
    private final List<Judge> judges = new LinkedList<>();

    public static VerdictStubBuilder verdict() {
        return new VerdictStubBuilder();
    }

    public VerdictStubBuilder AC(long runtime, long memoryUsage, int grade) {
        return judge(AC, runtime, memoryUsage, grade);
    }

    public VerdictStubBuilder SYSTEM_ERROR(long runtime, long memoryUsage) {
        return judge(SYSTEM_ERR, runtime, memoryUsage, 0);
    }

    public VerdictStubBuilder WA(long runtime, long memoryUsage) {
        return judge(WA, runtime, memoryUsage, 0);
    }

    public VerdictStubBuilder TLE(long runtime, long memoryUsage) {
        return judge(TLE, runtime, memoryUsage, 0);
    }

    public VerdictStubBuilder MLE(long runtime, long memoryUsage) {
        return judge(MLE, runtime, memoryUsage, 0);
    }

    public VerdictStubBuilder RE(long runtime, long memoryUsage) {
        return judge(RE, runtime, memoryUsage, 0);
    }

    public VerdictStubBuilder CE() {
        return CE("Compile Error");
    }

    public VerdictStubBuilder CE(String compileError) {
        this.compileError = compileError;
        return this;
    }

    protected VerdictStubBuilder judge(JudgeStatus status, long runtime, long memoryUsage, int grade) {
        if (compileError != null) {
            throw new IllegalArgumentException("CE verdict can't have judges");
        }
        judges.add(new Judge("T", status, new ProgramProfile(runtime, memoryUsage, ""), grade));
        return this;
    }

    public Verdict build() {
        Verdict verdict;
        if (compileError == null) {
            verdict = new Verdict(judges);
        } else {
            verdict = Verdict.compileError("Compile Error");
        }
        verdict.setReport(ProblemStubs.compositeReport());
        return verdict;
    }

}
