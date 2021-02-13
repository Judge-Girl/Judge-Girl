package tw.waterball.judgegirl.problemapi.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.problem.*;

import java.util.ArrayList;
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
    public List<LanguageEnv> languageEnvs;
    public JudgePluginTag judgeMatchPolicyPluginTag;
    public Collection<JudgePluginTag> judgeFilterPluginTags;
    public List<String> tags;
    public String testcaseIOsFileId;

    public static ProblemView fromEntity(Problem problem) {
        return new ProblemView(
                problem.getId(),
                problem.getTitle(),
                problem.getDescription(),
                new ArrayList<>(problem.getLanguageEnvs()),
                problem.getOutputMatchPolicyPluginTag(),
                problem.getFilterPluginTags(),
                problem.getTags(),
                problem.getTestcaseIOsFileId()
        );
    }

    public static Problem toEntity(ProblemView view) {
        var builder =  Problem.builder()
                .id(view.getId())
                .title(view.getTitle())
                .description(view.markdownDescription)
                .outputMatchPolicyPluginTag(view.judgeMatchPolicyPluginTag)
                .tags(view.tags)
                .testcaseIOsFileId(view.testcaseIOsFileId);
        if (view.judgeFilterPluginTags != null) {
            builder.filterPluginTags(view.judgeFilterPluginTags);
        }
        Problem problem = builder.build();
        for (LanguageEnv languageEnv : view.languageEnvs) {
            problem.addLanguageEnv(languageEnv);
        }
        return problem;
    }
}
