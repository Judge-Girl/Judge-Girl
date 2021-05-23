package tw.waterball.judgegirl.springboot.problem.repositories.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.Compilation;
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.ResourceSpec;

import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LanguageEnvData {
    public Language language;
    public String compilationScript;
    public float cpu;
    public float gpu;
    public List<SubmittedCodeSpecData> submittedCodeSpecs;
    public String providedCodesFileId;

    public static LanguageEnvData toData(LanguageEnv env) {
        return new LanguageEnvData(env.getLanguage(),
                env.getCompilation().getScript(),
                env.getResourceSpec().getCpu(),
                env.getResourceSpec().getGpu(),
                mapToList(env.getSubmittedCodeSpecs(), SubmittedCodeSpecData::toData),
                env.getProvidedCodesFileId());
    }

    public LanguageEnv toValue() {
        return new LanguageEnv(language,
                new Compilation(compilationScript),
                new ResourceSpec(cpu, gpu),
                mapToList(submittedCodeSpecs, SubmittedCodeSpecData::toValue),
                providedCodesFileId);
    }
}
