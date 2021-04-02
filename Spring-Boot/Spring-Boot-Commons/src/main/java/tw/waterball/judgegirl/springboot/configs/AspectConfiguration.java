package tw.waterball.judgegirl.springboot.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
@EnableAspectJAutoProxy
public class AspectConfiguration {

    public AspectConfiguration() {
        System.out.println();
    }
}
