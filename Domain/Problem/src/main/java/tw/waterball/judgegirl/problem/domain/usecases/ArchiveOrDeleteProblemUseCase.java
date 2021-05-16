package tw.waterball.judgegirl.problem.domain.usecases;

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

    public void execute(int problemId) {
        problemRepository.findProblemById(problemId)
                .ifPresent(this::archiveOrDeleteProblem);
    }

    private void archiveOrDeleteProblem(Problem problem) {
        if (problem.isArchived()) {
            problemRepository.deleteProblem(problem);
        } else {
            problemRepository.archiveProblem(problem);
        }
    }

}
