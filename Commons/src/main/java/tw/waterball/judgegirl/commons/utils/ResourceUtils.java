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

package tw.waterball.judgegirl.commons.utils;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ResourceUtils {
    public static File getFile(String resourcePath) {
        URL url = getURL(resourcePath);
        return new File(url.getFile());
    }

    public static Path getAbsolutePath(String resourcePath) {
        URL url = getURL(resourcePath);
        return Paths.get(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8));
    }

    public static URL getURL(String resourcePath) {
        URL url = ResourceUtils.class.getResource(resourcePath);
        if (url == null) {
            throw new RuntimeException("Resource not found at path: " + resourcePath);
        }
        return url;
    }

    public static InputStream getResourceAsStream(String resourcePath) {
        InputStream in = ResourceUtils.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("Resource not found in the path: " + resourcePath);
        }
        return in;
    }
}
