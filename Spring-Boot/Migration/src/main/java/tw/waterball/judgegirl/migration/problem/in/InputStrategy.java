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

package tw.waterball.judgegirl.migration.problem.in;

import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.ResourceSpec;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface InputStrategy {
    String problemDirPath();

    ResourceSpec resourceSpec();

    JudgeGirlMatchPolicyPlugin matchPolicyPlugin(JudgeGirlMatchPolicyPlugin[] matchPolicyPlugins);

    Optional<Problem> replaceExistingProblemOrNot(List<Problem> existingProblems);

    OptionalInt specifyProblemIdOrNot(List<Problem> existingProblems);
}
