/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.commons.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class DateUtils {
    private static final Calendar NEVER_EXPIRED_IN_LIFETIME_CALENDAR = Calendar.getInstance();
    public static final Date NEVER_EXPIRED_IN_LIFETIME_DATE = NEVER_EXPIRED_IN_LIFETIME_CALENDAR.getTime();

    static {
        NEVER_EXPIRED_IN_LIFETIME_CALENDAR.set(2100, Calendar.AUGUST, 7);
    }

    public static Date oneSecondAgo() {
        return beforeCurrentTime(1, TimeUnit.SECONDS);
    }

    public static Date afterCurrentTime(int unit, TimeUnit timeUnit) {
        long now = System.currentTimeMillis();
        long after = timeUnit.toMillis(unit);
        return new Date(now + after);
    }

    public static Date beforeCurrentTime(int unit, TimeUnit timeUnit) {
        long now = System.currentTimeMillis();
        long before = timeUnit.toMillis(unit);
        return new Date(now - before);
    }

}
