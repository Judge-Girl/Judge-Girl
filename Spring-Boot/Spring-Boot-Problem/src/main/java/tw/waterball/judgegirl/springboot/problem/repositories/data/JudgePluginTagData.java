package tw.waterball.judgegirl.springboot.problem.repositories.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JudgePluginTagData {
    private JudgePluginTag.Type type;
    private String group;
    private String name;
    private String version;

    public static JudgePluginTagData toData(JudgePluginTag tag) {
        return new JudgePluginTagData(
                tag.getType(),
                tag.getGroup(),
                tag.getName(),
                tag.getVersion());
    }

    public JudgePluginTag toValue() {
        return new JudgePluginTag(type, group, name, version);
    }

}
