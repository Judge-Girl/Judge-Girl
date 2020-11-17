package tw.waterball.judgegirl.problemservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.problemservice.domain.repositories.TestCaseRepository;

import javax.inject.Named;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetTestCasesUseCase {
    private TestCaseRepository testCaseRepository;

    public GetTestCasesUseCase(TestCaseRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    public void execute(Request request, Presenter presenter) throws NotFoundException {
        List<Testcase> testcases = testCaseRepository.findAllInProblem(request.problemId);
        presenter.setTestcases(testcases);
    }

    public interface Presenter {
        void setTestcases(List<Testcase> testcases);
    }

    @Value
    public static class Request {
        public int problemId;
    }
}
