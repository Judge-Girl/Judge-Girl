package tw.waterball.judgegirl.problemservice.domain.usecases;

import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;

@Named
public class ArchiveOrDeleteProblemUseCase extends BaseProblemUseCase {

    public ArchiveOrDeleteProblemUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public void execute(int problemId) {
        problemRepository.archiveProblemById(problemId);
    }

}
