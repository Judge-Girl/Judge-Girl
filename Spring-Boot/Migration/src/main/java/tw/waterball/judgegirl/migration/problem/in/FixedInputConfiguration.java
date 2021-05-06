package tw.waterball.judgegirl.migration.problem.in;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.primitives.problem.ResourceSpec;

import java.nio.file.Paths;
import java.util.OptionalInt;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Profile("fixed-input")
@Configuration
public class FixedInputConfiguration {

    @Bean
    @Primary
    public MigrateOneProblemStandardInput migrateOneProblemStandardInput() {
        int problemId = 50179;
        return MigrateOneProblemStandardInput.builder()
                .problemId(problemId)
                .compilationScript("gcc -O2 -std=c99 a.c")
                .tags(new String[]{"FILE I/O"})
                .legacyPackageRootPath(Paths.get("/Users/johnny850807/Documents/Judge-Girl-Migration/50179/50179"))
                .outputDirectoryPath(Paths.get("temp/migration/" + problemId))
                .resourceSpec(new ResourceSpec(1, 0))
                .matchPolicyPlugin(new AllMatchPolicyPlugin())
                .replaceExistingProblemOrNot(problems -> problems.stream()
                        .filter(p -> p.getId() == problemId).findFirst())
                .specifyProblemIdOrNot((problems) -> OptionalInt.of(problemId))
                .build();
    }
}
