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

import lombok.Value;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.entities.problem.LanguageEnv;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.problemservice.domain.repositories.ProblemRepository;

import javax.inject.Named;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class DownloadProvidedCodesUseCase extends BaseProblemUseCase {

    public DownloadProvidedCodesUseCase(ProblemRepository problemRepository) {
        super(problemRepository);
    }

    public FileResource execute(Request request) throws NotFoundException {
        Problem problem = doFindProblemById(request.problemId);
        LanguageEnv languageEnv = problem.getLanguageEnv(request.langEnvName);
        if (languageEnv.getProvidedCodesFileId().equals(request.providedCodesFileId)) {
            return problemRepository.downloadProvidedCodes(request.problemId, request.langEnvName)
                    .orElseThrow(() -> new NotFoundException(request.problemId, "problem"));
        }
        throw new IllegalArgumentException(
                String.format("Invalid provided codes' file id: %s.", request.providedCodesFileId));

    }

    @Value
    public static class Request {
        public int problemId;
        public String langEnvName;
        public String providedCodesFileId;
    }

}
