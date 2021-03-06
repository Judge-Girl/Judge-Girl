package tw.waterball.judgegirl.migration.ci;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class CI {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        ProblemClassificationSheet sheet = new ProblemClassificationSheet();
        sheet.connect();

        int normalProblemCount = 0;
        for (String name : sheet.getNames()) {
            for (Record record : sheet.getRecords(name)) {
                if (hasCompleted(record) && isNormalProblem(record)) {
                    System.out.println(record);
                    normalProblemCount++;
                }
            }
        }
        System.out.printf("Normal problem: %d.\n", normalProblemCount);
    }

    private static boolean hasCompleted(Record record) {
        return record.isValid();
    }

    private static boolean isNormalProblem(Record record) {
        return !record.isFileIn() && !record.isFileOut()
                && !record.isMuExec() && !record.isCmdPT();
    }
}
