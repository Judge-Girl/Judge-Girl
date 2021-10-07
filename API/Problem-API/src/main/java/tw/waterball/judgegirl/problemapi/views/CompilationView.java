package tw.waterball.judgegirl.problemapi.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.Compilation;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationView {
    public String script;

    public static CompilationView toViewModel(Compilation compilation) {
        return new CompilationView(compilation.getScript());
    }

    public Compilation toValue() {
        return new Compilation(script);
    }
}
