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

package tw.waterball.judgegirl.springboot.student.advices;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tw.waterball.judgegirl.commons.token.TokenInvalidException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateEmailException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentEmailNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentIdNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentPasswordIncorrectException;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@ControllerAdvice
public class StudentExceptionAdvices {
    public StudentExceptionAdvices() {
    }

    @ExceptionHandler({StudentPasswordIncorrectException.class, DuplicateEmailException.class, IllegalArgumentException.class})
    public ResponseEntity<?> badRequestHandler(Exception err) {
        return ResponseEntity.badRequest().body(err.getMessage());
    }

    @ExceptionHandler({StudentIdNotFoundException.class, StudentEmailNotFoundException.class})
    public ResponseEntity<?> notFoundHandler(Exception err) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({TokenInvalidException.class})
    public ResponseEntity<?> unauthorizedHandler(Exception err) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
