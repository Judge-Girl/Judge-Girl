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
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;
import tw.waterball.judgegirl.problem.domain.repositories.TestCaseRepository;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class DownloadTestCaseIOsUseCase extends BaseProblemUseCase {
    private final TestCaseRepository testCaseRepository;

    public DownloadTestCaseIOsUseCase(ProblemRepository problemRepository,
                                      TestCaseRepository testCaseRepository) {
        super(problemRepository);
        this.testCaseRepository = testCaseRepository;
    }

    public FileResource execute(Request request) throws NotFoundException {
        Problem problem = findProblem(request.problemId);
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
