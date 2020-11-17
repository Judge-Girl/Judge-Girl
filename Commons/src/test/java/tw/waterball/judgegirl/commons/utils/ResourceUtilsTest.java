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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static java.nio.charset.Charset.defaultCharset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceUtilsTest {
    private static final String expectedText = "This is for Test";

    @Test
    void testGetFile() throws IOException {
        File file = ResourceUtils.getFile("/test/Test.txt");
        String actualText = FileUtils.readFileToString(file, defaultCharset());
        assertEquals(expectedText, actualText);
    }

    @Test
    void testGetAbsolutePath() throws IOException {
        Path absolutePath = ResourceUtils.getAbsolutePath("/test/Test.txt");
        assertTrue(absolutePath.startsWith("/"),
                "Should start with '/' since it's absolute.");
        String actualText = IOUtils.toString(new FileInputStream(absolutePath.toFile()), defaultCharset());
        assertEquals(expectedText, actualText);
    }

    @Test
    void testGetResourceAsStream() throws IOException {
        InputStream stream = ResourceUtils.getResourceAsStream("/test/Test.txt");
        String actualText = IOUtils.toString(stream, defaultCharset());
        assertEquals(expectedText, actualText);
    }
}