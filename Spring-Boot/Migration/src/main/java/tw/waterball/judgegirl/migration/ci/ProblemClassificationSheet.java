package tw.waterball.judgegirl.migration.ci;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Iterator;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@Component
public class ProblemClassificationSheet extends GoogleSheet {
    private final static String[] fields = {"Subdomain", "Problem's ID", "Problem's Title", "Tags (Split by ',')"
            , "Compilation Script", "FI: File-In", "FO: File-Out", "MU-EXEC: Multiple-Executables",
            "CMD-P-T: Command per Testcase", "LM: Link to Math lib", "Note", "Completion"};
    private final static String[] names = {"魏宏軒", "盧冠維", "陳安泰", "李昭妤", "Joe Chang",
            "Cheng Hung Wu", "林建宏", "王甯", "杜秉翰", "邱麒羽"};
    private Sheets sheets;

    @Override
    public Sheets connect() throws GeneralSecurityException, IOException {
        sheets = super.connect();
        verify(sheets);
        return sheets;
    }

    private void verify(Sheets sheets) throws IOException {
        for (String name : names) {

            ValueRange valueRange = sheets.spreadsheets().values()
                    .get(spreadsheetId, "'" + name + "'!A1:L1")
                    .execute();
            var values = valueRange.getValues();
            for (int i = 0; i < values.size(); i++) {
                if (!values.get(0).get(i).equals(fields[i])) {
                    throw new IllegalStateException(
                            String.format("[%s] Invalid, the %d field is: %s, expected: %s  ", name, i, values.get(i).get(0), fields[i]));
                }
            }
        }

        log.info("Verification passed.");
    }

    public Iterable<Record> getRecords(String name) throws IOException {
        ValueRange valueRange = sheets.spreadsheets().values()
                .get(spreadsheetId, "'" + name + "'!A:L")
                .execute();
        var rows = valueRange.getValues();
        var records = rows.subList(1, rows.size());
        return () -> new Iterator<>() {
            int index = 0;
            @Override
            public boolean hasNext() {
                return index < records.size();
            }
            @Override
            public Record next() {
                var row = records.get(index++);
                return new Record(String.valueOf(row.get(0)),
                        String.valueOf(row.get(1)),
                        String.valueOf(row.get(2)),
                        String.valueOf(row.get(3)).split("\\s,\\s"),
                        String.valueOf(row.get(4)),
                        yseOrNo(avoidIndexOutOfBound(row, 5)),
                        yseOrNo(avoidIndexOutOfBound(row, 6)),
                        yseOrNo(avoidIndexOutOfBound(row, 7)),
                        yseOrNo(avoidIndexOutOfBound(row, 8)),
                        yseOrNo(avoidIndexOutOfBound(row, 9)),
                        String.valueOf(avoidIndexOutOfBound(row, 10)),
                        yseOrNo(avoidIndexOutOfBound(row, 11)));
            }

            private boolean yseOrNo(Object field) {
                String str = String.valueOf(field).trim();
                if (str.isEmpty()) {
                    return false;
                }
                return str.equalsIgnoreCase("yes") || str.equalsIgnoreCase("true");
            }

            private Object avoidIndexOutOfBound(List<Object> row, int index) {
                if (index < row.size()) {
                    return row.get(index);
                }
                return "";
            }
        };
    }


    public String[] getNames() {
        return names;
    }
}

