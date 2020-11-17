package tw.waterball.judgegirl.problemservice.domain.usecases;

import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetProblemListUseCase {
    private ProblemRepository problemRepository;

    public GetProblemListUseCase(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public void execute(ProblemQueryParams problemQueryParams, Presenter presenter) {
        List<Problem> problems = problemRepository.find(problemQueryParams);
        presenter.setProblemList(problems);
    }

    public interface Presenter {
        void setProblemList(List<Problem> problems);
    }
}
