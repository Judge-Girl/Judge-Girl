package tw.waterball.judgegirl.springboot.amqp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.springboot.profiles.productions.Amqp;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Amqp
@Configuration
public class MessageConverterConfiguration {
    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
