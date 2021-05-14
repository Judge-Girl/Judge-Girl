package tw.waterball.judgegirl.primitives.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static tw.waterball.judgegirl.commons.utils.DateUtils.oneSecondAgo;
import static tw.waterball.judgegirl.primitives.time.DateProvider.now;
import static tw.waterball.judgegirl.primitives.time.Duration.during;

class DurationTest {

    @Test
    void startTimeShouldBeBeforeEndTime() {
        assertThrows(IllegalStateException.class, () ->
                during(now(), oneSecondAgo()));
    }
}