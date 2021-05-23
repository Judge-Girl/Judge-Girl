package tw.waterball.judgegirl.springboot.problem.repositories.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.Testcase;

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

    public static TestcaseData toData(Testcase testcase) {
        return new TestcaseData(
                testcase.getId(),
                testcase.getName(),
                testcase.getProblemId(),
                testcase.getTimeLimit(),
                testcase.getMemoryLimit(),
                testcase.getOutputLimit(),
                testcase.getThreadNumberLimit(),
                testcase.getGrade());
    }

    public Testcase toValue() {
        return new Testcase(id, name, problemId, timeLimit, memoryLimit, outputLimit, threadNumberLimit, grade);
    }
}
