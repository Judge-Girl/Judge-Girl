package tw.waterball.judgegirl.springboot.problem.repositories.data;

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
public class SubmittedCodeSpecData {
    public Language format;
    public String fileName;

    public static SubmittedCodeSpecData toData(SubmittedCodeSpec spec) {
        return new SubmittedCodeSpecData(spec.getFormat(), spec.getFileName());
    }

    public SubmittedCodeSpec toValue() {
        return new SubmittedCodeSpec(format, fileName);
    }
}
