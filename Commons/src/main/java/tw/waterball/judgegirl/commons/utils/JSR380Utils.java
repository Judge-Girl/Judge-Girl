/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.commons.utils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class JSR380Utils {
    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public static boolean isValid(Object object) {
        return getViolations(object).isEmpty();
    }

    public static void validate(Object object) {
        Set<ConstraintViolation<Object>> violations = getViolations(object);
        if (!violations.isEmpty()) {
            throw new EntityInvalidException(violations);
        }
    }

    public static Set<ConstraintViolation<Object>> getViolations(Object object) {
        Validator validator = factory.getValidator();
        return validator.validate(object);
    }

    public static class EntityInvalidException extends IllegalArgumentException {
        private final Set<ConstraintViolation<Object>> violations;

        EntityInvalidException(Set<ConstraintViolation<Object>> violations) {
            super(violations.stream()
                    .map(Object::toString).collect(Collectors.joining("\n")));
            this.violations = violations;
        }

        public Set<ConstraintViolation<Object>> getViolations() {
            return violations;
        }
    }
}
