package tw.waterball.judgegirl.problemapi.controllers.views;

import lombok.Value;
import tw.waterball.judgegirl.commons.entities.*;

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
    public String zippedProvidedCodesFileId;
    public String zippedTestCaseInputsFileId;
    public String zippedTestCaseOutputsFileId;

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
                problem.getZippedProvidedCodesFileId(),
                problem.getZippedTestCaseInputsFileId(),
                problem.getZippedTestCaseOutputsFileId()
        );
    }
}
