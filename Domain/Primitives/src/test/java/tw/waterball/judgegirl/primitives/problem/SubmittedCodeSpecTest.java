package tw.waterball.judgegirl.primitives.problem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.waterball.judgegirl.commons.utils.StringUtils.generateStringOfLength;

class SubmittedCodeSpecTest {

    @Test
    void EmptyFileNameShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new SubmittedCodeSpec(Language.C, ""));
    }

    @Test
    void LongFileNameOfLength300ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new SubmittedCodeSpec(Language.C, generateStringOfLength('c', 300)));
    }

}