package tw.waterball.judgegirl.problemapi.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.problem.*;

import java.util.Collection;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemView {
    public Integer id;
    public String title;
    public String markdownDescription;
    public JudgeEnvSpec judgeEnvSpec;
    public JudgePluginTag judgeMatchPolicyPluginTag;
    public Collection<JudgePluginTag> judgeFilterPluginTags;
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
                problem.getJudgeEnvSpec(),
                problem.getOutputMatchPolicyPluginTag(),
                problem.getFilterPluginTags(),
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
        var builder =  Problem.builder()
                .id(view.getId())
                .title(view.getTitle())
                .markdownDescription(view.markdownDescription)
                .judgeEnvSpec(view.judgeEnvSpec)
                .outputMatchPolicyPluginTag(view.judgeMatchPolicyPluginTag)
                .inputFileNames(view.inputFileNames)
                .outputFileNames(view.outputFileNames)
                .tags(view.tags)
                .submittedCodeSpecs(view.submittedCodeSpecs)
                .compilation(view.compilation)
                .providedCodesFileId(view.providedCodesFileId)
                .testcaseIOsFileId(view.testcaseIOsFileId);
        if (view.judgeFilterPluginTags != null) {
            builder.filterPluginTags(view.judgeFilterPluginTags);
        }
        return builder.build();
    }
}
