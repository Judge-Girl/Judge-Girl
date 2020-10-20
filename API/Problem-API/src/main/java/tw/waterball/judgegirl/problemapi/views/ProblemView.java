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
    public JudgePluginTag judgeMatchPolicyPluginTag;
    public JudgePluginTag judgeCodeInspectionPluginTag;
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
                problem.getOutputMatchPolicyPluginTag(),
                problem.getCodeInspectionPluginTag().orElse(null),
                problem.getInputFileNames(),
                problem.getOutputFileNames(),
                problem.getTags(),
                problem.getSubmittedCodeSpecs(),
                problem.getCompilation(),
                problem.getProvidedCodesFileId(),
                problem.getTestcaseIOsFileId()
        );
    }

    public static Problem toEntity(ProblemView view) {
        return Problem.builder()
                .id(view.getId())
                .title(view.getTitle())
                .markdownDescription(view.markdownDescription)
                .judgeSpec(view.judgeSpec)
                .outputMatchPolicyPluginTag(view.judgeMatchPolicyPluginTag)
                .inputFileNames(view.inputFileNames)
                .outputFileNames(view.outputFileNames)
                .tags(view.tags)
                .submittedCodeSpecs(view.submittedCodeSpecs)
                .compilation(view.compilation)
                .providedCodesFileId(view.providedCodesFileId)
                .testcaseIOsFileId(view.testcaseIOsFileId)
                .build();
    }
}
