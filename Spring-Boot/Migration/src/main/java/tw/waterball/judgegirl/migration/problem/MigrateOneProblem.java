package tw.waterball.judgegirl.migration.problem;

import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@SpringBootApplication(scanBasePackages = "tw.waterball.judgegirl")
@AllArgsConstructor
public class MigrateOneProblem {
    private final ConvertLegacyLayout convertLegacyLayout;
    private final PopulateOneProblem populateOneProblem;

    public void execute() throws Exception {
        convertLegacyLayout.execute();
        populateOneProblem.execute();
    }

    public static void main(String[] args) {
        var context = SpringApplication.run(MigrateOneProblem.class);
        try {
            MigrateOneProblem migrateOneProblem = context.getBean(MigrateOneProblem.class);
            migrateOneProblem.execute();
            System.out.println("Completed.");
            SpringApplication.exit(context, () -> 1);
        } catch (Exception err) {
            err.printStackTrace();
            SpringApplication.exit(context, () -> -1);
        }
    }

    public interface Input extends ConvertLegacyLayout.Input, PopulateOneProblem.Input {
    }

}
