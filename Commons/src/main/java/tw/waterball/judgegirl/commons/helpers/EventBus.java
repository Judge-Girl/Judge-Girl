package tw.waterball.judgegirl.commons.helpers;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class EventBus {
    private final List<Handler> handlers;

    public EventBus(List<Handler> handlers) {
        this.handlers = handlers;
    }

    public void publish(Object event) {
        handlers.forEach(h -> h.handle(event));
    }

    public interface Handler {
        void handle(Object event);
    }
}
