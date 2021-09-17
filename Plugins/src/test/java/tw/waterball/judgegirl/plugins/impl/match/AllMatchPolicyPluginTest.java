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

package tw.waterball.judgegirl.plugins.impl.match;

import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.commons.utils.ResourceUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AllMatchPolicyPluginTest {
    private final AllMatchPolicyPlugin policyPlugin = new AllMatchPolicyPlugin();

    @Test
    void testAC() {
        Path fileName = ResourceUtils.getAbsolutePath("/stub/test/file1");
        assertTrue(policyPlugin.isMatch(
                fileName, fileName,
                singletonMap(fileName, fileName)));
    }

    @Test
    void testWA_differInStandardOut() {
        Path file1 = ResourceUtils.getAbsolutePath("/stub/test/file1");
        Path file2 = ResourceUtils.getAbsolutePath("/stub/test/file2");
        assertFalse(policyPlugin.isMatch(
                file1, file2,
                singletonMap(file1, file1)));
    }

    @Test
    void testWA_differInOutputFile() {
        Path file1 = ResourceUtils.getAbsolutePath("/stub/test/file1");
        Path file2 = ResourceUtils.getAbsolutePath("/stub/test/file2");
        assertFalse(policyPlugin.isMatch(file1, file1,
                singletonMap(file1, file2)));
    }

    @Test
    void testWA_differInOutputFile_ButManyOthersAreCorrect() {
        // prepare files
        Map<Path, Path> actualToExpectOutputFileNameMap = new HashMap<>();
        File testDir = ResourceUtils.getAbsolutePath("/stub/test").toFile();
        File[] testFiles = Arrays.stream(requireNonNull(testDir.listFiles()))
                .filter(File::isFile).toArray(File[]::new);
        for (File testFile : testFiles) {
            actualToExpectOutputFileNameMap.put(
                    testFile.toPath(), testFile.toPath());
        }
        // adjust to make only one of the output-file-pair match failed
        // this test aims at issuing whether this file-pair's match failure is accurately detected
        actualToExpectOutputFileNameMap.put(
                testFiles[testFiles.length - 1].toPath(),
                testFiles[0].toPath());

        assertFalse(policyPlugin.isMatch(
                testFiles[0].toPath(), testFiles[0].toPath(),
                actualToExpectOutputFileNameMap));
    }
}