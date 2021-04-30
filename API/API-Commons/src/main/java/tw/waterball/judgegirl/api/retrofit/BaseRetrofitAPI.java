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

package tw.waterball.judgegirl.api.retrofit;

import lombok.RequiredArgsConstructor;
import okhttp3.Headers;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Call;
import retrofit2.Response;
import tw.waterball.judgegirl.api.exceptions.ApiRequestFailedException;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.utils.HttpHeaderUtils;
import tw.waterball.judgegirl.commons.utils.functional.IoErrSupplier;

import java.io.IOException;
import java.util.function.Supplier;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static tw.waterball.judgegirl.api.retrofit.BaseRetrofitAPI.ExceptionDeclaration.mapStatusCode;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class BaseRetrofitAPI {
    private final Logger logger = LogManager.getLogger(getClass());

    protected final <T> Response<T> validateResponse(Response<T> response,
                                                     ExceptionDeclaration... exceptionDeclarations) throws NotFoundException, ApiRequestFailedException {
        logger.debug("Response: " + response);
        final int code = response.code();
        if (!response.isSuccessful()) {
            var exceptionDeclaration = stream(exceptionDeclarations).filter(d -> d.errorCode == code)
                    .findFirst().orElseGet(defaultExceptionDeclarations(response, code));
            exceptionDeclaration.throwIt();
        }
        return response;
    }

    private <T> Supplier<ExceptionDeclaration> defaultExceptionDeclarations(Response<T> response, int code) {
        return () -> {
            if (code == 404) {
                return mapStatusCode(404).toThrow(() -> new NotFoundException(response.message()));
            } else {
                return mapStatusCode(code).toThrow(() -> ApiRequestFailedException.failed(response.code(), response.message()));
            }
        };
    }

    protected FileResource parseDownloadedFileResource(Response<ResponseBody> response) {
        Headers headers = response.headers();
        String fileName = HttpHeaderUtils.parseFileNameFromContentDisposition(
                requireNonNull(headers.get("Content-Disposition")));
        return new FileResource(fileName, Long.parseLong(requireNonNull(headers.get("Content-Length"))),
                requireNonNull(response.body()).byteStream());
    }

    protected final <T> T errorHandlingGetBody(IoErrSupplier<Response<T>> responseErrSupplier, ExceptionDeclaration... exceptionDeclarations) {
        try {
            return validateResponse(responseErrSupplier.get(), exceptionDeclarations).body();
        } catch (IOException e) {
            throw ApiRequestFailedException.connectionError(e);
        }
    }

    protected final <T> Response<T> errorHandlingGetResponse(IoErrSupplier<Call<T>> responseErrSupplier, ExceptionDeclaration... exceptionDeclarations) {
        try {
            return validateResponse(responseErrSupplier.get().execute(), exceptionDeclarations);
        } catch (IOException e) {
            throw ApiRequestFailedException.connectionError(e);
        }
    }
    
    @RequiredArgsConstructor
    public static class ExceptionDeclaration {
        public final int errorCode;
        public Supplier<RuntimeException> exceptionSupplier = RuntimeException::new;

        public static ExceptionDeclaration mapStatusCode(int errorCode) {
            return new ExceptionDeclaration(errorCode);
        }

        public ExceptionDeclaration toThrow(Supplier<RuntimeException> exceptionSupplier) {
            this.exceptionSupplier = exceptionSupplier;
            return this;
        }

        public void throwIt() {
            throw exceptionSupplier.get();
        }
    }
}
