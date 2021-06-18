package tw.waterball.judgegirl.primitives.time;

import java.util.Date;

/**
 * All domain module's code must use this class to get the current time (now).
 * So that we can switch to the different date to represent "now".
 * <p>
 * This is useful when we are going to test our domain module.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public final class DateProvider {
    private static Date now;

    public static void setNow(Date now) {
        DateProvider.now = (Date) now.clone();
    }

    public static Date now() {
        return now == null ? new Date() : now;
    }
}
