package tw.waterball.judgegirl.problemapi.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;

import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageEnvView {
    public String name;
    public Language language;
    public CompilationView compilation;
    public ResourceSpecView resourceSpec;
    public List<SubmittedCodeSpecView> submittedCodeSpecs;
    public String providedCodesFileId;

    public static LanguageEnvView toViewModel(LanguageEnv env) {
        return new LanguageEnvView(env.getName(), env.getLanguage(),
                CompilationView.toViewModel(env.getCompilation()),
                ResourceSpecView.toViewModel(env.getResourceSpec()),
                mapToList(env.getSubmittedCodeSpecs(), SubmittedCodeSpecView::toData),
                env.getProvidedCodesFileId());
    }

    public LanguageEnv toValue() {
        return new LanguageEnv(language,
                compilation.toValue(),
                resourceSpec.toValue(),
                mapToList(submittedCodeSpecs, SubmittedCodeSpecView::toValue),
                providedCodesFileId);
    }
}
