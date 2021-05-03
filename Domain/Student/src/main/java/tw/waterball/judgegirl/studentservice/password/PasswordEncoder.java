package tw.waterball.judgegirl.studentservice.password;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface PasswordEncoder {

    String encode(CharSequence rawPassword);

    boolean matches(CharSequence raw, String encoded);
}
