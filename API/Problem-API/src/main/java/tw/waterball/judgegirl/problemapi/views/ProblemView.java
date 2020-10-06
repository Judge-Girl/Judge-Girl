package tw.waterball.judgegirl.problemapi.views;

import lombok.Value;
import tw.waterball.judgegirl.entities.problem.*;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Value
public class ProblemView {
    public Integer id;
    public String title;
    public String markdownDescription;
    public JudgeSpec judgeSpec;
    public JudgePluginTag judgePolicyPluginTag;
    public List<String> inputFileNames;
    public List<String> outputFileNames;
    public List<String> tags;
    public List<SubmittedCodeSpec> submittedCodeSpecs;
    public Compilation compilation;
    public String providedCodesFileId;
    public String testcaseIOsFileId;

    public static ProblemView fromEntity(Problem problem) {
        return new ProblemView(
                problem.getId(),
                problem.getTitle(),
                problem.getMarkdownDescription(),
                problem.getJudgeSpec(),
                problem.getJudgePolicyPluginTag(),
                problem.getInputFileNames(),
                problem.getOutputFileNames(),
                problem.getTags(),
                problem.getSubmittedCodeSpecs(),
                problem.getCompilation(),
                problem.getProvidedCodesFileId(),
                problem.getTestcaseIOsFileId()
        );
    }
}
