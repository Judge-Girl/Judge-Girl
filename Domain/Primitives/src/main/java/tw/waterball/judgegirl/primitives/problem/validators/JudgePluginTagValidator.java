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

package tw.waterball.judgegirl.primitives.problem.validators;

import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class JudgePluginTagValidator implements
        ConstraintValidator<JudgePluginTagConstraint, JudgePluginTag> {
    private JudgePluginTag.Type[] expectTypes;

    @Override
    public void initialize(JudgePluginTagConstraint constraintAnnotation) {
        expectTypes = constraintAnnotation.typeShouldBe();
    }

    @Override
    public boolean isValid(JudgePluginTag tag, ConstraintValidatorContext context) {
        return tag == null ||
                Arrays.stream(expectTypes).anyMatch(t -> t == tag.getType());
    }
}
