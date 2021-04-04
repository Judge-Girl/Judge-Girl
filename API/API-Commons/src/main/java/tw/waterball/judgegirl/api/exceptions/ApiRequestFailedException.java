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

package tw.waterball.judgegirl.api.exceptions;

import java.io.IOException;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ApiRequestFailedException extends RuntimeException {
    private int errorCode;
    private String message = "";
    private boolean networkingError = false;

    public static ApiRequestFailedException connectionError(IOException err) {
        return new ApiRequestFailedException(err);
    }

    public static ApiRequestFailedException failed(int errorCode, String message) {
        return new ApiRequestFailedException(errorCode, message);
    }

    private ApiRequestFailedException(IOException err) {
        super(err);
        this.networkingError = true;
    }

    private ApiRequestFailedException(int errorCode, String message) {
        super("Error code: " + errorCode + ", message: " + message);
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public boolean isNetworkingError() {
        return networkingError;
    }
}
