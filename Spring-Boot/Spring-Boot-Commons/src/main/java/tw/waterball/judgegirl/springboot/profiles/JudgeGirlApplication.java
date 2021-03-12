package tw.waterball.judgegirl.springboot.profiles;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.annotation.*;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootApplication(scanBasePackages = "tw.waterball.judgegirl")
public @interface JudgeGirlApplication {
}
