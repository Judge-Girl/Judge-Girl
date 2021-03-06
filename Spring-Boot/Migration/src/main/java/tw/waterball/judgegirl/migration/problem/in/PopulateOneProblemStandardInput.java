package tw.waterball.judgegirl.migration.problem.in;

import tw.waterball.judgegirl.commons.utils.Inputs;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.ResourceSpec;
import tw.waterball.judgegirl.migration.problem.PopulateOneProblem;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPlugin;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.commons.utils.Inputs.*;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class PopulateOneProblemStandardInput implements PopulateOneProblem.Input {
    @Override
    public String problemDirPath() {
        return inputLine("Problem Dir");
    }

    @Override
    public ResourceSpec resourceSpec() {
        return new ResourceSpec(inputRangedNumberOrDefault("CPU", 1, 0, 5),
                inputRangedNumberOrDefault("GPU", 0, 0, 4));
    }

    @Override
    public JudgeGirlPlugin matchPolicyPlugin(JudgeGirlMatchPolicyPlugin[] matchPolicyPlugins) {
        for (int i = 0; i < matchPolicyPlugins.length; i++) {
            System.out.printf("[%d] -- %s%n", i, matchPolicyPlugins[i].getTag());
        }
        int selection = inputRangedIntegerOrDefault("Please select the plugin: ", 0, 0, matchPolicyPlugins.length-1);
        return matchPolicyPlugins[selection];
    }

    @Override
    public Optional<Problem> replaceExistingProblemOrNot(List<Problem> problems) {
        if (inputForYesOrNo("Would you like to replace the existing problem?")) {
            String options = problems.stream().map(p -> String.format("[%d] %s", p.getId(), p.getTitle()))
                    .collect(Collectors.joining("\n"));
            List<Integer> idList = problems.stream().map(Problem::getId).collect(Collectors.toList());
            options += "\nPlease select a problem id";
            int input = Inputs.inputRangedInteger(options, idList);
            return problems.stream().filter(p -> p.getId() == input).findFirst();
        } else {
            return Optional.empty();
        }
    }

    @Override
    public OptionalInt specifyProblemIdOrNot(List<Problem> problems) {
        if (inputForYesOrNo("Would you like to specify the problem's id")) {
            int problemId = Inputs.inputConditionalInteger("Input the problem's id",
                    id -> id >= 0 && problems.stream().noneMatch(p -> p.getId().equals(id)),
                    id -> String.format("The input id %d must not exist and be positive.", id));
            return OptionalInt.of(problemId);
        } else {
            return OptionalInt.empty();
        }
    }
}
