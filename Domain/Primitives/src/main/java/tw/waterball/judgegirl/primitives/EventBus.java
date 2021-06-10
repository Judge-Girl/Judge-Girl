package tw.waterball.judgegirl.primitives;

import lombok.extern.slf4j.Slf4j;
import tw.waterball.judgegirl.primitives.submission.events.Event;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
public class EventBus {
    private final List<Handler> handlers;

    public EventBus(List<Handler> handlers) {
        this.handlers = handlers;
    }

    public void publish(Event event) {
        log.info("[Produce: {}] {}", event.getName(), event.toString());
        handlers.forEach(h -> h.handle(event));
    }

    public interface Handler {
        void handle(Object event);
    }
}
