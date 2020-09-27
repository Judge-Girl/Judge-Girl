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

import okhttp3.Headers;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import retrofit2.Call;
import retrofit2.Response;
import tw.waterball.judgegirl.api.exceptions.ApiConnectionException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.utils.HttpHeaderUtils;
import tw.waterball.judgegirl.commons.utils.functional.ErrSupplier;

import static java.util.Objects.requireNonNull;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class BaseRetrofitServiceAPI {
    private Logger logger = LogManager.getLogger(getClass());

    protected <T> Response<T> validateResponse(Response<T> response) {
        logger.debug("Response: " + response);
        if (!response.isSuccessful()) {
            throw new ApiConnectionException("Error code: " + response.code() + ", message: " + response.message());
        }
        return response;
    }

    protected FileResource parseDownloadedFileResource(Response<ResponseBody> response) {
        Headers headers = response.headers();
        String fileName = HttpHeaderUtils.parseFileNameFromContentDisposition(
                requireNonNull(headers.get("Content-Disposition")));
        return new FileResource(fileName, Long.parseLong(requireNonNull(headers.get("Content-Length"))),
                requireNonNull(response.body()).byteStream());
    }

    protected <T> T errorHandlingGetBody(ErrSupplier<Response<T>> responseErrSupplier) {
        try {
            return validateResponse(responseErrSupplier.get()).body();
        } catch (Exception e) {
            throw new ApiConnectionException(e);
        }
    }

    protected <T> Response<T> errorHandlingGetResponse(ErrSupplier<Call<T>> responseErrSupplier) {
        try {
            return validateResponse(responseErrSupplier.get().execute());
        } catch (Exception e) {
            throw new ApiConnectionException(e);
        }
    }
}
