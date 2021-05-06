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

package tw.waterball.judgegirl.problemapi.clients;

import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface ProblemServiceDriver {

    ProblemView getProblem(int problemId) throws NotFoundException;

    default FileResource downloadProvidedCodes(int problemId, LanguageEnv languageEnv) throws NotFoundException {
        return downloadProvidedCodes(problemId, languageEnv.getName(), languageEnv.getProvidedCodesFileId());
    }

    FileResource downloadProvidedCodes(int problemId, String languageEnvName, String providedCodesFileId) throws NotFoundException;

    FileResource downloadTestCaseIOs(int problemId, String testcaseIOsFileId) throws NotFoundException;
}
