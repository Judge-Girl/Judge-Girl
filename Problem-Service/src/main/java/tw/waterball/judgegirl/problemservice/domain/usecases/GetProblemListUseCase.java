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

package tw.waterball.judgegirl.problemservice.domain.usecases;

import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemQueryParams;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;
import java.util.Arrays;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class GetProblemListUseCase {
    private final ProblemRepository problemRepository;

    public GetProblemListUseCase(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public void execute(ProblemQueryParams problemQueryParams, Presenter presenter) {
        List<Problem> problems = problemRepository.find(problemQueryParams);
        presenter.showProblems(problems);
    }

    public void execute(int[] problemIds, Presenter presenter) {
        presenter.showProblems(problemRepository.findProblemsByIds(problemIds));
    }

    public interface Presenter {
        void showProblems(List<Problem> problems);
    }
}
