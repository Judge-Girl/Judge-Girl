/**
 * @author swshawnwu@gmail.com(ShawnWu)
 */

package tw.waterball.judgegirl.springboot.problem.repositories.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.Testcase;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.toMap;

/**
 * @author - swshawnwu@gmail.com (Shawn)
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document("problem")
public class ProblemData {

    @Id
    private Integer id;

    private String title;

    private String description;

    private Map<String, LanguageEnv> languageEnvs;

    private JudgePluginTag outputMatchPolicyPluginTag;

    private Collection<JudgePluginTag> filterPluginTags;

    private List<String> tags = new ArrayList<>();

    private Map<String, Testcase> testcases = new HashMap<>();

    private boolean visible;

    private String testcaseIOsFileId;

    private boolean archived;

    public static ProblemData toData(Problem problem) {
        return ProblemData.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .languageEnvs(problem.getLanguageEnvs())
                .outputMatchPolicyPluginTag(problem.getOutputMatchPolicyPluginTag())
                .filterPluginTags(problem.getFilterPluginTags())
                .tags(problem.getTags())
                .testcases(toMap(problem.getTestcases(), Testcase::getId, identity()))
                .visible(problem.getVisible())
                .testcaseIOsFileId(problem.getTestcaseIOsFileId())
                .archived(problem.getVisible())
                .build();
    }

    public Problem toEntity() {
        return new Problem(id, title, description, languageEnvs, outputMatchPolicyPluginTag,
                new HashSet<>(filterPluginTags), tags,
                getTestCases(), visible, testcaseIOsFileId, archived);
    }

    public List<Testcase> getTestCases() {
        return testcases != null ? new ArrayList<>(testcases.values()) : emptyList();
    }

    public LanguageEnv getLanguageEnv(String name) {
        return languageEnvs.get(name);
    }
}
