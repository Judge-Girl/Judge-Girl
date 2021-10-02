package tw.waterball.judgegirl.springboot.problem.repositories.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.*;

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
    public List<String> providedCodesFileNames;

    public static LanguageEnvData toData(LanguageEnv env) {
        var providedCodes = env.getProvidedCodes();
        return new LanguageEnvData(env.getLanguage(),
                env.getCompilation().getScript(),
                env.getResourceSpec().getCpu(),
                env.getResourceSpec().getGpu(),
                mapToList(env.getSubmittedCodeSpecs(), SubmittedCodeSpecData::toData),
                providedCodes.map(ProvidedCodes::getProvidedCodesFileId).orElse(null),
                providedCodes.map(ProvidedCodes::getProvidedCodesFileName).orElse(null));
    }

    public LanguageEnv toValue() {
        ProvidedCodes providedCodes = null;
        if (providedCodesFileId != null && providedCodesFileNames != null) {
            providedCodes = new ProvidedCodes(providedCodesFileId, providedCodesFileNames);
        }

        return new LanguageEnv(language,
                new Compilation(compilationScript),
                new ResourceSpec(cpu, gpu),
                mapToList(submittedCodeSpecs, SubmittedCodeSpecData::toValue),
                providedCodes);
    }
}
