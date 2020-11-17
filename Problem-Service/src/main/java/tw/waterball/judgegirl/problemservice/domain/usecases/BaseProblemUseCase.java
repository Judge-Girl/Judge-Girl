package tw.waterball.judgegirl.problemservice.domain.usecases;

import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class BaseProblemUseCase {
    protected ProblemRepository problemRepository;

    public BaseProblemUseCase(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    protected Problem doFindProblemById(int problemId) throws NotFoundException {
        return problemRepository.findProblemById(problemId)
                .orElseThrow(() -> new NotFoundException(problemId, "problem"));
    }
}
