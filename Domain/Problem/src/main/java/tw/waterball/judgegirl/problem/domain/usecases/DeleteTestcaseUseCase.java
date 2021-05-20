package tw.waterball.judgegirl.problem.domain.usecases;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;

/**
 * @author swshawnwu@gmail.com(ShawnWu)
 */

@Named
public class DeleteTestcaseUseCase {

    private final ProblemRepository testCaseRepository;

    public DeleteTestcaseUseCase(ProblemRepository testCaseRepository) {
        this.testCaseRepository = testCaseRepository;
    }

    public void execute(Request request) throws NotFoundException {
        testCaseRepository.deleteTestcaseById(request.problemId, request.testcaseId);
    }


    @AllArgsConstructor
    public static class Request {
        public int problemId;
        public String testcaseId;
    }

}