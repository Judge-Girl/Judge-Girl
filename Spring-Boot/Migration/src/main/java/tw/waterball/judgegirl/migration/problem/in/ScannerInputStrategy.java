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

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.utils.Inputs;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.ResourceSpec;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ConditionalOnMissingBean(InputStrategy.class)
@Component
public class ScannerInputStrategy implements InputStrategy {
    @Override
    public String problemDirPath() {
        return Inputs.inputLine("Input the problem directory path: ");
    }

    @Override
    public ResourceSpec resourceSpec() {
        return new ResourceSpec(
                Inputs.inputRangedNumberOrDefault("Input allocated CPU ", 1f, 0.1f, 100),
                Inputs.inputRangedNumberOrDefault("Input allocated GPU ", 0f, 0, 10));
    }

    @Override
    public JudgeGirlMatchPolicyPlugin matchPolicyPlugin(JudgeGirlMatchPolicyPlugin[] matchPolicyPlugins) {
        String message = "Please select the plugin: \n\n";
        message += IntStream.range(0, matchPolicyPlugins.length)
                .mapToObj(i -> format("[%d] -- %s", i, matchPolicyPlugins[i].getTag()))
                .collect(Collectors.joining("\n"));

        int index = Inputs.inputRangedIntegerOrDefault(message, 0, 0, matchPolicyPlugins.length - 1);
        return matchPolicyPlugins[index];
    }

    @Override
    public Optional<Problem> replaceExistingProblemOrNot(List<Problem> existingProblems) {
        if (Inputs.inputForYesOrNo("Would you like to replace the existing problem?")) {
            String options = existingProblems.stream().map(p -> format("[%d] %s", p.getId(), p.getTitle()))
                    .collect(Collectors.joining("\n"));
            List<Integer> idList = existingProblems.stream().map(Problem::getId).collect(Collectors.toList());
            options += "\nPlease select a problem id";
            int input = Inputs.inputRangedInteger(options, idList);
            return existingProblems.stream().filter(p -> p.getId() == input).findFirst();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public OptionalInt specifyProblemIdOrNot(List<Problem> existingProblems) {
        if (Inputs.inputForYesOrNo("Would you like to specify the problem's id")) {
            int problemId = Inputs.inputConditionalInteger("Input the problem's id",
                    id -> id >= 0 && existingProblems.stream().noneMatch(p -> p.getId().equals(id)),
                    id -> format("The input id %d must not exist and be positive.", id));
            return OptionalInt.of(problemId);
        } else {
            return OptionalInt.empty();
        }
    }
}
