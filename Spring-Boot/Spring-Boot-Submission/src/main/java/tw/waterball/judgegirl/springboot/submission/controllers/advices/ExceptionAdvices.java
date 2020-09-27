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

package tw.waterball.judgegirl.springboot.submission.controllers.advices;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import tw.waterball.judgegirl.submissionservice.domain.usecases.exceptions.SubmissionThrottlingException;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ControllerAdvice
public class ExceptionAdvices {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({SubmissionThrottlingException.class})
    public void handleExceptions() {
        // Nothing to do
    }
}
