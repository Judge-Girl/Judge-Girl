package tw.waterball.judgegirl.problem.domain.usecases;

import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class RestoreProblemUseCase extends BaseProblemUseCase {

    public RestoreProblemUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public void execute(int problemId) throws NotFoundException {
        var problem = findProblem(problemId);
        problemRepository.restoreProblem(problem);
    }
}
