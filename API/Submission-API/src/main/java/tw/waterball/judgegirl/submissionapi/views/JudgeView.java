package tw.waterball.judgegirl.submissionapi.views;

import lombok.*;
import tw.waterball.judgegirl.primitives.grading.Grade;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.verdict.Judge;
import tw.waterball.judgegirl.primitives.submission.verdict.ProgramProfile;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class JudgeView {
    public String testcaseName;
    public JudgeStatus status;
    public ProgramProfile programProfile;
    public int grade;
    public int maxGrade;

    public static JudgeView toViewModel(Judge judge) {
        return new JudgeView(judge.getTestcaseName(),
                judge.getStatus(), judge.getProgramProfile(),
                judge.getGrade(), judge.getMaxGrade());
    }

    public Judge toEntity() {
        return new Judge(testcaseName, status, programProfile, new Grade(grade, maxGrade));
    }
}
