package tw.waterball.judgegirl.problem.domain.usecases;

import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;

@Named
public class SaveProblemWithTitleUseCase extends BaseProblemUseCase {
    public SaveProblemWithTitleUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public int execute(String title) {
        return problemRepository.saveProblemWithTitleAndGetId(title);
    }
}
