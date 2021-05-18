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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

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
        var problem = findProblem(request.problemId);
        notFoundIfTheProblemIsInvisible(request, problem);
        presenter.setProblem(problem);
    }

    private void notFoundIfTheProblemIsInvisible(Request request, Problem problem) {
        if (!request.includeInvisibleProblem && !problem.getVisible()) {
            throw NotFoundException.notFound(Problem.class).id(request.problemId);
        }
    }

    public interface Presenter {
        void setProblem(Problem problem);
    }

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
    public static class Request {
        public final int problemId;
        public boolean includeInvisibleProblem;
    }
}
