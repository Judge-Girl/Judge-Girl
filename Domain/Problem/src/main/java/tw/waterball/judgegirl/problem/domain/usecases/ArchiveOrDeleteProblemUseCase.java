package tw.waterball.judgegirl.problem.domain.usecases;

import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;

/**
 * Archive the problem if it's not been archived, otherwise, delete it.
 *
 * @author - c11037at@gmail.com (snowmancc)
 */
@Named
public class ArchiveOrDeleteProblemUseCase extends BaseProblemUseCase {

    public ArchiveOrDeleteProblemUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public void execute(int problemId) throws NotFoundException {
        var problem = findProblem(problemId);
        archiveOrDeleteProblem(problem);
    }

    private void archiveOrDeleteProblem(Problem problem) {
        if (problem.isArchived()) {
            problemRepository.deleteProblem(problem);
        } else {
            problemRepository.archiveProblem(problem);
        }
    }

}
