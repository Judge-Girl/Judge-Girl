package tw.waterball.judgegirl.commons.utils.validations;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;
import java.util.regex.Pattern;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Documented
@Constraint(validatedBy = EnglishLetterOrDigitOrDash.Validator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnglishLetterOrDigitOrDash {

    String message() default "Every character should be [A-Za-z0-9] or a dash '-'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<EnglishLetterOrDigitOrDash, String> {
        private final Pattern pattern = Pattern.compile("[A-Za-z0-9\\-]*");

        @Override
        public boolean isValid(String text, ConstraintValidatorContext constraintValidatorContext) {
            return pattern.matcher(text).matches();
        }
    }

}
