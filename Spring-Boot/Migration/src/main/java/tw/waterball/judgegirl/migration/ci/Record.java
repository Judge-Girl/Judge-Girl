package tw.waterball.judgegirl.migration.ci;

import lombok.Value;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Value
public class Record {
    @NotBlank
    String subdomain;
    @NotBlank
    String problemId;
    @NotBlank
    String problemTitle;
    @NotNull
    String[] tags;
    @NotBlank
    String compilationScript;
    boolean fileIn;
    boolean fileOut;
    boolean muExec;
    boolean cmdPT;
    boolean linkToMath;
    String note;
    boolean completion;

    public boolean isValid() {
        return JSR380Utils.isValid(this);
    }
}
