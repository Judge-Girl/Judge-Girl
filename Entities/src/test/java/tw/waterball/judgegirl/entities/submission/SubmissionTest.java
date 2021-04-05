package tw.waterball.judgegirl.entities.submission;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static tw.waterball.judgegirl.entities.stubs.SubmissionStubBuilder.submission;

class SubmissionTest {

    @Test
    void GivenAllACWithSameGrade_bestRecordShouldHaveTheLowestValueOfRuntimePlusMemoryUsage() {
        bestRecordShouldBe("B",
                submission("A").AC(10, 3000, 100),
                submission("B").AC(30, 50, 100),
                submission("C").AC(3000, 10, 100));
    }

    @Test
    void GivenWA_RE_MLE_bestRecordShouldHaveHighestGrade() {
        bestRecordShouldBe("A",
                submission("A").WA(0, 0)
                        .AC(30, 50, 10)
                        .AC(30, 50, 10)
                        .AC(30, 50, 10),

                submission("B").RE(0, 0)
                        .RE(0, 0)
                        .AC(3000, 10, 10)
                        .AC(3000, 10, 10),

                submission("C").MLE(0, 0)
                        .MLE(0, 0)
                        .MLE(0, 0)
                        .AC(10, 3000, 10));
    }

    @Test
    void GivenWA_RE_CE_bestRecordShouldNotBeCE() {
        bestRecordShouldNotBe("A",
                submission("A").CE(),
                submission("B").WA(0, 0),
                submission("C").RE(0, 0));
    }

    @Test
    void GivenWA_MLE_SYSTEMERROR_bestRecordShouldNotBeSystemError() {
        bestRecordShouldNotBe("B",
                submission("A").WA(0, 0),
                submission("B").SYSTEM_ERROR(0, 0),
                submission("C").MLE(0, 0));
    }

    @Test
    void bestRecordShouldHaveBeenJudged() {
        bestRecordShouldBe("B",
                submission("A"),
                submission("B").RE(0, 0),
                submission("C"));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends Comparable> void bestRecordShouldBe(String expectedId, T... submissions) {
        Object bestRecord = Collections.max(Arrays.asList(submissions));
        assertEquals(expectedId, ((Submission) bestRecord).getId());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T extends Comparable> void bestRecordShouldNotBe(String expectedId, T... submissions) {
        Object bestRecord = Collections.max(Arrays.asList(submissions));
        assertNotEquals(expectedId, ((Submission) bestRecord).getId());
    }

}