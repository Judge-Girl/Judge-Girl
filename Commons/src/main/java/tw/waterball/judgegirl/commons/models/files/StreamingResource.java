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

package tw.waterball.judgegirl.commons.models.files;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@AllArgsConstructor
@Getter
@Setter
public class StreamingResource implements Closeable {
    protected String fileName;
    protected InputStream inputStream;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreamingResource that = (StreamingResource) o;
        try {
            //TODO: `contentEquals` will consume and exhaust the stream,
            //   we need to reset the stream to make the stream sill available
            return IOUtils.contentEquals(inputStream, that.inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Error occurs during measuring the equality between " +
                    "two input-stream, did you mis-invoke equals() method? The input-stream might have been consumed.", e);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, inputStream);
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
