package tw.waterball.judgegirl.problemservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.problemservice.domain.repositories.TestCaseRepository;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class DownloadTestCaseIOsUseCase extends BaseProblemUseCase {
    private TestCaseRepository testCaseRepository;

    public DownloadTestCaseIOsUseCase(ProblemRepository problemRepository,
                                      TestCaseRepository testCaseRepository) {
        super(problemRepository);
        this.testCaseRepository = testCaseRepository;
    }

    public FileResource execute(Request request) throws NotFoundException {
        Problem problem = doFindProblemById(request.problemId);
        if (problem.getTestcaseIOsFileId().equals(request.testcaseIOsFileId)) {
            return testCaseRepository.downloadTestCaseIOs(request.problemId, request.testcaseIOsFileId)
                    .orElseThrow(() -> new NotFoundException(request.problemId, "problem"));
        }
        throw new IllegalArgumentException(
                String.format("Invalid testcase IO's file id: %s.", request.testcaseIOsFileId));
    }

    @Value
    public static class Request {
        public int problemId;
        public String testcaseIOsFileId;
    }
}
