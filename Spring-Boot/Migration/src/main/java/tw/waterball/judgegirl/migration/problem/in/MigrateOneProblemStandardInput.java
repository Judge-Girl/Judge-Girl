package tw.waterball.judgegirl.migration.problem.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import tw.waterball.judgegirl.migration.problem.MigrateOneProblem;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.ResourceSpec;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@AllArgsConstructor
public class MigrateOneProblemStandardInput implements MigrateOneProblem.Input {
    private final int problemId;
    private final Path legacyPackageRootPath;
    private final Path outputDirectoryPath;
    private final String compilationScript;
    private final String[] tags;
    private final ResourceSpec resourceSpec;
    private final JudgeGirlMatchPolicyPlugin matchPolicyPlugin;
    private final Function<List<Problem>, Optional<Problem>> replaceExistingProblemOrNot;
    private final Function<List<Problem>, OptionalInt> specifyProblemIdOrNot;

    @Override
    public int problemId() {
        return problemId;
    }

    @Override
    public Path legacyPackageRootPath() {
        return legacyPackageRootPath;
    }

    @Override
    public Path outputDirectoryPath() {
        return outputDirectoryPath;
    }

    @Override
    public String compilationScript() {
        return compilationScript;
    }

    @Override
    public String[] tags() {
        return tags;
    }

    @Override
    public String problemDirPath() {
        return outputDirectoryPath.toAbsolutePath().toString();
    }

    @Override
    public ResourceSpec resourceSpec() {
        return resourceSpec;
    }

    @Override
    public JudgeGirlMatchPolicyPlugin matchPolicyPlugin(JudgeGirlMatchPolicyPlugin[] matchPolicyPlugins) {
        return matchPolicyPlugin;
    }

    @Override
    public Optional<Problem> replaceExistingProblemOrNot(List<Problem> existingProblems) {
        return replaceExistingProblemOrNot.apply(existingProblems);
    }

    @Override
    public OptionalInt specifyProblemIdOrNot(List<Problem> existingProblems) {
        return specifyProblemIdOrNot.apply(existingProblems);
    }
}
