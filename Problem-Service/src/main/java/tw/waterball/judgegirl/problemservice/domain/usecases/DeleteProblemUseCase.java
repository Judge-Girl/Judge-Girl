package tw.waterball.judgegirl.problemservice.domain.usecases;

import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;

@Named
public class DeleteProblemUseCase extends BaseProblemUseCase {

    public DeleteProblemUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public void execute(int problemId) {
        problemRepository.deleteProblemById(problemId);
    }

}
