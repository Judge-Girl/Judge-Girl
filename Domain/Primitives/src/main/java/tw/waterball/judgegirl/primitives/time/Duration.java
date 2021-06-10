package tw.waterball.judgegirl.primitives.time;

import java.util.Date;

import static tw.waterball.judgegirl.commons.utils.DateUtils.format;
import static tw.waterball.judgegirl.primitives.time.DateProvider.now;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Duration {
    private final Date start;
    private final Date end;

    public Duration(Date start, Date end) {
        this.start = start;
        this.end = end;
        if (start.after(end)) {
            throw new IllegalStateException("The start time should not exceed the end time.");
        }
    }

    public static Duration during(Date start, Date end) {
        return new Duration(start, end);
    }

    public Date getStartTime() {
        return start;
    }

    public Date getEndTime() {
        return end;
    }

    public boolean isUpcoming() {
        return getStartTime().after(now());
    }

    public boolean isPast() {
        return getEndTime().before(now());
    }

    public boolean isOngoing() {
        Date now = now();
        return getStartTime().before(now) && getEndTime().after(now);
    }

    @Override
    public String toString() {
        return String.format("%s ~ %s", format(start), format(end));
    }
}
