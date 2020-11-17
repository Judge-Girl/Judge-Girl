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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipUtilsTest {

    @Test
    void testUnzipToDestination() throws IOException {
        Path destinationPath = Files.createTempDirectory("judge-girl-test");
        File destination = destinationPath.toFile();

        ZipUtils.unzipToDestination(
                ResourceUtils.getResourceAsStream("/test.zip"), destinationPath);

        // test the unzipped artifact has the exact content to the original one
        assertTrue(DirectoryUtils.contentEquals(destination.toPath(),
                ResourceUtils.getAbsolutePath("/test")));

        FileUtils.forceDelete(destination);
    }

    @Test
    void testZipToFile() throws IOException {
        File file = ResourceUtils.getFile("/test");
        File zip = File.createTempFile("judge-girl-test", ".zip");
        zip.deleteOnExit();

        ZipUtils.zipToFile(file, new FileOutputStream(zip));

        Path destinationPath = Files.createTempDirectory("judge-girl-test");
        ZipUtils.unzipToDestination(new FileInputStream(zip), destinationPath);

        assertTrue(DirectoryUtils.contentEquals(
                file.toPath(), destinationPath.resolve("test")));

        FileUtils.forceDelete(destinationPath.toFile());
    }

    @Test
    void GivenZipFilesFromResources_thenUnzip_contentShouldEqualTheOriginalContent() throws IOException {
        byte[] bytes = IOUtils.toByteArray(ResourceUtils.getResourceAsStream("/test/A/debug.c"));
        byte[] zip = ZipUtils.zipFilesFromResources("/test/A/debug.c");
        byte[] unzip = ZipUtils.unzipFirst(new ByteArrayInputStream(zip));

        assertArrayEquals(bytes, unzip);
    }

}