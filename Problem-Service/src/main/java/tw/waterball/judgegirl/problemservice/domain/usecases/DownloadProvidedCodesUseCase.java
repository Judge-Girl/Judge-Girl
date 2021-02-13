package tw.waterball.judgegirl.problemservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.problem.LanguageEnv;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class DownloadProvidedCodesUseCase extends BaseProblemUseCase {

    public DownloadProvidedCodesUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public FileResource execute(Request request) throws NotFoundException {
        Problem problem = doFindProblemById(request.problemId);
        LanguageEnv languageEnv = problem.getLanguageEnv(request.langEnvName);
        if (languageEnv.getProvidedCodesFileId().equals(request.providedCodesFileId)) {
            return problemRepository.downloadProvidedCodes(request.problemId, request.langEnvName)
                    .orElseThrow(() -> new NotFoundException(request.problemId, "problem"));
        }
        throw new IllegalArgumentException(
                String.format("Invalid provided codes' file id: %s.", request.providedCodesFileId));

    }

    @Value
    public static class Request {
        public int problemId;
        public String langEnvName;
        public String providedCodesFileId;
    }

}
