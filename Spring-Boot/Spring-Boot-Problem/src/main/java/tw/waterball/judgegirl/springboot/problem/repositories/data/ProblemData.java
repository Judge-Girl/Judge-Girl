package tw.waterball.judgegirl.springboot.problem.repositories.data;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.Testcase;

import java.util.*;

import static java.util.Collections.emptyList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.*;

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
    private Map<String, LanguageEnvData> languageEnvs;
    private JudgePluginTagData outputMatchPolicyPluginTag;
    private Collection<JudgePluginTagData> filterPluginTags;
    private List<String> tags = new ArrayList<>();
    private Map<String, TestcaseData> testcases = new HashMap<>();
    private boolean visible;
    private String testcaseIOsFileId;
    private boolean archived;

    public static ProblemData toData(Problem problem) {
        return ProblemData.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .languageEnvs(toMap(problem.getLanguageEnvs().entrySet(),
                        Map.Entry::getKey, e -> LanguageEnvData.toData(e.getValue())))
                .outputMatchPolicyPluginTag(
                        JudgePluginTagData.toData(problem.getOutputMatchPolicyPluginTag()))
                .filterPluginTags(mapToList(problem.getFilterPluginTags(), JudgePluginTagData::toData))
                .tags(problem.getTags())
                .testcases(toMap(problem.getTestcases(), Testcase::getId, TestcaseData::toData))
                .visible(problem.getVisible())
                .testcaseIOsFileId(problem.getTestcaseIOsFileId())
                .archived(problem.isArchived())
                .build();
    }

    public Problem toEntity() {
        return new Problem(id, title, description,
                toMap(languageEnvs.entrySet(),
                        Map.Entry::getKey, e -> e.getValue().toValue()),
                outputMatchPolicyPluginTag.toValue(),
                mapToSet(filterPluginTags, JudgePluginTagData::toValue), tags,
                getTestCaseList(), visible, archived, testcaseIOsFileId);
    }

    private List<Testcase> getTestCaseList() {
        return testcases != null ? mapToList(testcases.values(), TestcaseData::toValue)
                : emptyList();
    }

    public LanguageEnvData getLanguageEnv(String name) {
        return languageEnvs.get(name);
    }
}
