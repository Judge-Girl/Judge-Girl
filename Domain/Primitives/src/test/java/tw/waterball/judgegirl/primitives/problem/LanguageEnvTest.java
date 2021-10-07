package tw.waterball.judgegirl.primitives.problem;

import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.waterball.judgegirl.commons.utils.StringUtils.generateStringOfLength;
import static tw.waterball.judgegirl.primitives.stubs.ProblemStubs.languageEnvTemplate;

class LanguageEnvTest {

    @Test
    void LongProvidedCodesFileIdOfLength1000ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> languageEnvTemplate(Language.C)
                        .providedCodes(new ProvidedCodes(generateStringOfLength('c', 1000), singletonList("FileName")))
                        .build());
    }

}