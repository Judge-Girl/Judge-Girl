package tw.waterball.judgegirl.springboot.submission.impl.mongo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.verdict.ProgramProfile;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgeData {
    private String testcaseName;
    private JudgeStatus status;
    private ProgramProfile programProfile;
    private int grade;
    private int maxGrade;
}
