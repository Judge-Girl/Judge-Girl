package tw.waterball.judgegirl.entities.stubs;

import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.submission.verdict.Judge;
import tw.waterball.judgegirl.entities.submission.verdict.ProgramProfile;
import tw.waterball.judgegirl.entities.submission.verdict.Verdict;

import java.util.LinkedList;
import java.util.List;

import static tw.waterball.judgegirl.entities.problem.JudgeStatus.*;

/**
 * @author - wally55077@gmail.com
 */
public class VerdictStubBuilder {

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
        judges.add(new Judge("T", CE, ProgramProfile.onlyCompileError("error"), 0));
        return this;
    }

    protected VerdictStubBuilder judge(JudgeStatus status, long runtime, long memoryUsage, int grade) {
        judges.add(new Judge("T", status, new ProgramProfile(runtime, memoryUsage, ""), grade));
        return this;
    }
    
    public Verdict build() {
        Verdict verdict = new Verdict(judges);
        verdict.setReport(ProblemStubs.compositeReport());
        return verdict;
    }

}
