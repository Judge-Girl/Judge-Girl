package tw.waterball.judgegirl.primitives.date;

import java.util.Date;

/**
 * All domain module's code must use this class to provide date.
 * So that we can test them with specifying different dates.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public final class DateProvider {
    private static Date mockedNow = null;

    public static void setMockedNow(Date mockedNow) {
        DateProvider.mockedNow = mockedNow;
    }

    public static Date now() {
        return mockedNow == null ? new Date() : mockedNow;
    }
}
