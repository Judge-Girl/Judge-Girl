package tw.waterball.judgegirl.primitives.problem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.waterball.judgegirl.commons.utils.StringUtils.generateStringOfLength;

class CompilationTest {
    @Test
    void LongCompilationScriptWithLength1000ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new Compilation(generateStringOfLength('g', 1000)));

    }

    @Test
    void EmptyCompilationScriptShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new Compilation(""));

    }
}