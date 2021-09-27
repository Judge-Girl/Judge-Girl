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
import tw.waterball.judgegirl.problem.domain.repositories.ProblemRepository;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class DownloadTestCaseIOsUseCase extends BaseProblemUseCase {

    public DownloadTestCaseIOsUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public FileResource execute(Request request) throws NotFoundException {
        return problemRepository.downloadTestCaseIOs(request.problemId, request.testcaseId)
                .orElseThrow(() -> new NotFoundException(String.format("The testcase's IO is not found. (problemId=%d, testcaseId=%s) " +
                                "The problem or the testcase does not exist or the IO files has not been uploaded.",
                        request.problemId, request.testcaseId)));
    }

    @Value
    public static class Request {
        public int problemId;
        public String testcaseId;
    }
}
