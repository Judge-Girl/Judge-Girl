package tw.waterball.judgegirl.springboot.problem.repositories.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.Testcase;
import tw.waterball.judgegirl.primitives.problem.TestcaseIO;

import java.util.Optional;
import java.util.Set;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestcaseData {
    public String id;
    public String name;
    public int problemId;
    public int timeLimit;
    public long memoryLimit;
    public long outputLimit;
    public int threadNumberLimit;
    public int grade;
    public String ioFileId;
    public String stdIn;
    public String stdOut;
    public Set<String> inputFiles;
    public Set<String> outputFiles;

    public static TestcaseData toData(Testcase testcase) {
        Optional<TestcaseIO> io = testcase.getTestcaseIO();
        return new TestcaseData(
                testcase.getId(),
                testcase.getName(),
                testcase.getProblemId(),
                testcase.getTimeLimit(),
                testcase.getMemoryLimit(),
                testcase.getOutputLimit(),
                testcase.getThreadNumberLimit(),
                testcase.getGrade(),
                io.map(TestcaseIO::getId).orElse(null),
                io.map(TestcaseIO::getStdIn).orElse(null),
                io.map(TestcaseIO::getStdOut).orElse(null),
                io.map(TestcaseIO::getInputFiles).orElse(null),
                io.map(TestcaseIO::getOutputFiles).orElse(null));
    }

    public Testcase toValue() {
        var io = ioFileId == null ? null : new TestcaseIO(ioFileId, id, stdIn, stdOut, inputFiles, outputFiles);
        return new Testcase(id, name, problemId, timeLimit, memoryLimit, outputLimit, threadNumberLimit, grade, io);
    }
}
