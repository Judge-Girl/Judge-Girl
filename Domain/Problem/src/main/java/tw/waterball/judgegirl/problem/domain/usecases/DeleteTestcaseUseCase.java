package tw.waterball.judgegirl.problem.domain.usecases;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;

/**
 * @author swshawnwu@gmail.com(ShawnWu)
 */

@Named
@AllArgsConstructor
public class DeleteTestcaseUseCase {

    private final ProblemRepository problemRepository;

    public void execute(Request request) {
        problemRepository.deleteTestcaseById(request.problemId, request.testcaseId);
    }

    @AllArgsConstructor
    public static class Request {
        public int problemId;
        public String testcaseId;
    }

}