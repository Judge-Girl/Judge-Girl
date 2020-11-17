package tw.waterball.judgegirl.problemservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetProblemUseCase extends BaseProblemUseCase {
    public GetProblemUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public void execute(Request request, Presenter presenter) throws NotFoundException {
        presenter.setProblem(doFindProblemById(request.problemId));
    }

    public interface Presenter {
        void setProblem(Problem problem);
    }

    @Value
    public static class Request {
        public int problemId;
    }
}
