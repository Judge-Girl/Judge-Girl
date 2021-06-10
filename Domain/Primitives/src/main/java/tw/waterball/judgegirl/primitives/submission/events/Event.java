package tw.waterball.judgegirl.primitives.submission.events;

import lombok.EqualsAndHashCode;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@EqualsAndHashCode
public abstract class Event {
    protected String name;

    public Event(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
