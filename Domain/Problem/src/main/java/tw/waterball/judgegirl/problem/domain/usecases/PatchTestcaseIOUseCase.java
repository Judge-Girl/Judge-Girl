/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.problem.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.problem.Testcase;
import tw.waterball.judgegirl.primitives.problem.TestcaseIO;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class PatchTestcaseIOUseCase extends BaseProblemUseCase {

    public PatchTestcaseIOUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public void execute(Request request, Presenter presenter) {
        Problem problem = findProblem(request.problemId);
        validate(request, problem);

        problem = problemRepository.patchTestcaseIOs(problem, request.testcaseIoPatching);

        presenter.showResult(problem);
    }

    private void validate(Request request, Problem problem) {
        Testcase testcase = problem.getTestcaseById(request.testcaseId)
                .orElseThrow(() -> notFound(Testcase.class).id(request.testcaseId));
        testcase.getTestcaseIO().ifPresent(io -> io.apply(request.testcaseIoPatching));
    }

    public interface Presenter {

        void showResult(Problem problem);
    }

    @Value
    public static class Request {
        public int problemId;
        public String testcaseId;
        public TestcaseIO.IoPatching testcaseIoPatching;
    }
}
