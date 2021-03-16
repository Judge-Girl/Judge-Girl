package tw.waterball.judgegirl.problemservice.domain.usecases;

import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;

@Named
public class SaveProblemWithTitleUseCase extends BaseProblemUseCase {
    public SaveProblemWithTitleUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public int execute(String title) throws NotFoundException {
        return problemRepository.saveProblemWithTitleAndGetId(title);
    }
}
