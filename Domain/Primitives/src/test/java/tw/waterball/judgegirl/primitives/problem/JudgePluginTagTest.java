package tw.waterball.judgegirl.primitives.problem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.waterball.judgegirl.commons.utils.StringUtils.generateStringOfLength;
import static tw.waterball.judgegirl.primitives.problem.JudgePluginTag.Type.FILTER;

class JudgePluginTagTest {
    public static final String NAME = "Name";
    public static final String VERSION = "1.0";
    public static final String GROUP = "Group";

    @Test
    void LongGroupNameShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new JudgePluginTag(FILTER,
                        generateStringOfLength('c', 150), NAME, VERSION));
    }

    @Test
    void LongNameShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new JudgePluginTag(FILTER,
                        GROUP, generateStringOfLength('c', 150), VERSION));
    }

    @Test
    void LongVersionShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new JudgePluginTag(FILTER,
                        GROUP, NAME, generateStringOfLength('c', 150)));
    }

}