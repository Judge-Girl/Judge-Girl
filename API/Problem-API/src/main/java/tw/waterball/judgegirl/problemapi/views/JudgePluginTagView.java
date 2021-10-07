package tw.waterball.judgegirl.problemapi.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JudgePluginTagView {
    public JudgePluginTag.Type type;
    public String group;
    public String name;
    public String version;

    public static JudgePluginTagView toViewModel(JudgePluginTag tag) {
        return new JudgePluginTagView(tag.getType(), tag.getGroup(), tag.getName(), tag.getVersion());
    }

    public JudgePluginTag toEntity() {
        return new JudgePluginTag(type, group, name, version);
    }
}
