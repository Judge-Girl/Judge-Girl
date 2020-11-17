package tw.waterball.judgegirl.problemservice.domain.usecases;

import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetAllTagsUseCase {
    private ProblemRepository problemRepository;

    public GetAllTagsUseCase(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public List<String> execute() {
        return problemRepository.getTags();
    }
}
