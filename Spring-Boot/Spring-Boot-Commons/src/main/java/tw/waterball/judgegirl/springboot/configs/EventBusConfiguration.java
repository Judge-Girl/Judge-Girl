package tw.waterball.judgegirl.springboot.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.primitives.EventBus;

import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
public class EventBusConfiguration {
    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public EventBus eventBus(List<EventBus.Handler> handlers) {
        return new EventBus(handlers);
    }
}
