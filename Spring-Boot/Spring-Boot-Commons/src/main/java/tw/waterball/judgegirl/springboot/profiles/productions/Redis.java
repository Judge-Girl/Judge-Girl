package tw.waterball.judgegirl.springboot.profiles.productions;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import tw.waterball.judgegirl.springboot.profiles.Profiles;

import java.lang.annotation.*;

/**
 * @author - wally55077@gmail.com
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Primary
@Profile(Profiles.REDIS)
public @interface Redis {
}
