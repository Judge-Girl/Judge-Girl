package tw.waterball.judgegirl.commons.utils.validations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.waterball.judgegirl.commons.utils.validations.ValidationUtils.validate;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class EnglishLetterOrDigitOrDashValidatorTest {

    @Test
    void trailingSpaceShouldBeInvalid() {
        test("abcdefghijk ", false);
    }

    @Test
    void chineseShouldBeInvalid() {
        test("哈", false);
    }

    @Test
    void uuidShouldBeValid() {
        for (int i = 0; i < 10; i++) {
            test(UUID.randomUUID().toString(), true);
        }
    }

    @ParameterizedTest
    @CsvSource(value = {
            "a,true", "B,true", "4,true", "B13,true", "-,true", "12s90as78d09as7d,true",
            "a90sd709asd7,true", "a708d7-as08d-70as,true", "AaAaAaA-as8daus8-9d7as8d7a-08s7d-08a7s,true",
            "_,false", "asdasd_asd,false", " ,false", "a a,false", "__+,false",
            "1234567890),false", "@,false", ".,false", "a.c.d,false", "13=123+,false",
    })
    void test(String text, boolean valid) {
        if (valid) {
            assertDoesNotThrow(() -> new Tester(text), "Expect '" + text + "' to be valid.");
        } else {
            assertThrows(RuntimeException.class, () -> new Tester(text),
                    "Expect '" + text + "' to be invalid.");
        }
    }

    public static class Tester {
        @EnglishLetterOrDigitOrDash
        private final String text;

        public Tester(String text) {
            this.text = text;
            validate(this);
        }
    }
}
