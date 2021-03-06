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

package tw.waterball.judgegirl.entities.problem.validators;

import tw.waterball.judgegirl.entities.problem.JudgePluginTag;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Documented
@Constraint(validatedBy = JudgePluginTagValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JudgePluginTagConstraint {

    JudgePluginTag.Type[] typeShouldBe();

    String message() default "Invalid Judge Plugin Tag";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
