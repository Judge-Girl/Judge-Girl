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

package tw.waterball.judgegirl.springboot.advices;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tw.waterball.judgegirl.api.exceptions.ApiRequestFailedException;
import tw.waterball.judgegirl.commons.exceptions.ForbiddenAccessException;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.token.TokenInvalidException;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.status;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Slf4j
@ControllerAdvice
@Order
public class CommonExceptionAdvices {
    public CommonExceptionAdvices() {
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<?> handleNotFoundException(Exception err) {
        if (log.isDebugEnabled()) {
            log.debug("[Resource Not Found] {}", err.getMessage());
        }
        return status(NOT_FOUND).body(err.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
    public ResponseEntity<?> handleIllegalExceptions(Exception err) {
        log.warn("[Illegal operation] {}", err.getMessage());
        return status(BAD_REQUEST).body(err.getMessage());
    }

    @ExceptionHandler({TokenInvalidException.class})
    public ResponseEntity<?> handleTokenInvalidException(TokenInvalidException err) {
        log.warn("[Invalid Token] {}", err.getMessage());
        return status(UNAUTHORIZED).body("Invalid Token");
    }

    @ExceptionHandler({ForbiddenAccessException.class})
    public ResponseEntity<?> handleForbiddenAccessException(ForbiddenAccessException err) {
        log.warn("[Forbidden Access] {}", err.getMessage());
        return status(FORBIDDEN).body(err.getMessage());
    }

    @ExceptionHandler({ApiRequestFailedException.class})
    public ResponseEntity<?> handleApiRequestFailedException(ApiRequestFailedException err) {
        if (err.isNetworkingError()) {
            log.error("[Api Failure]", err);
            return status(INTERNAL_SERVER_ERROR).build();
        } else {
            log.warn("[API Failure] {}", err.getMessage());
        }
        return status(err.getErrorCode()).body(err.getMessage());
    }
}
