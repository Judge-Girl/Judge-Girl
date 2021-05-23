package tw.waterball.judgegirl.problemapi.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.SubmittedCodeSpec;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmittedCodeSpecView {
    public Language format;
    public String fileName;

    public static SubmittedCodeSpecView toData(SubmittedCodeSpec spec) {
        return new SubmittedCodeSpecView(spec.getFormat(), spec.getFileName());
    }

    public SubmittedCodeSpec toValue() {
        return new SubmittedCodeSpec(format, fileName);
    }
}
