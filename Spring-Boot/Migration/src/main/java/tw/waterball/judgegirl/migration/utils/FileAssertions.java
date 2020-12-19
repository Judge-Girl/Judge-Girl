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

package tw.waterball.judgegirl.migration.utils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class FileAssertions {
    public static void assertDirectoryExists(Path dirPath) {
        if (!Files.exists(dirPath)) {
            throw new IllegalStateException(String.format("The path %s must exist. \n", dirPath));
        }
        if (!Files.isDirectory(dirPath)) {
            throw new IllegalStateException(String.format("%s must be a directory. \n", dirPath));
        }
    }

    public static void assertFileExists(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new IllegalStateException(String.format("The path %s must exist. \n", filePath));
        }
        if (!Files.isRegularFile(filePath)) {
            throw new IllegalStateException(String.format("%s must be a directory. \n", filePath));
        }
    }
}
