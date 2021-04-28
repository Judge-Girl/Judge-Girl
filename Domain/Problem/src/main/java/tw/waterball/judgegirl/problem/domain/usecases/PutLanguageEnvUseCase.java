package tw.waterball.judgegirl.problem.domain.usecases;


import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

@Named
public class PutLanguageEnvUseCase extends BaseProblemUseCase {
    public PutLanguageEnvUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public void execute(int problemId, String langEnv, LanguageEnv languageEnv) throws NotFoundException {
        if (problemRepository.problemExists(problemId)) {
            problemRepository.replaceProblemLanguageEnvByIdAndLangEnv(problemId, langEnv, languageEnv);

        } else {
            throw notFound(Problem.class).id(problemId);
        }
    }
}
