package tw.waterball.judgegirl.primitives.submission.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@AllArgsConstructor
@EqualsAndHashCode
public abstract class Event {
    protected String name;

    public String getName() {
        return name;
    }
}
