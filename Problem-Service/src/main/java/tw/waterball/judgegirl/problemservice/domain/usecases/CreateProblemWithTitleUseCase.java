package tw.waterball.judgegirl.problemservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.stubs.ProblemStubs;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;

@Named
public class CreateProblemWithTitleUseCase extends BaseProblemUseCase {
    public CreateProblemWithTitleUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public int execute(Request request) throws NotFoundException {
        return problemRepository.createProblemAndGetId(ProblemStubs.template().title(request.title).build());
    }

    @Value
    public static class Request {
        public String title;
    }
}
