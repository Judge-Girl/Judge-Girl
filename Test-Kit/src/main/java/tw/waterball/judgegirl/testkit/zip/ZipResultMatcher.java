/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.testkit.zip;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ZipResultMatcher implements ResultMatcher {
    private Map<String, MultipartFile> fileNameToFileMap = new HashMap<>();

    public ZipResultMatcher(MultipartFile[] files) {
        for (MultipartFile file : files) {
            fileNameToFileMap.put(file.getOriginalFilename(), file);
        }
    }

    @Override
    public void match(MvcResult result) throws Exception {
        HashSet<String> fileNameSet = new HashSet<>(fileNameToFileMap.keySet());
        MockHttpServletResponse response = result.getResponse();
        byte[] bytes = response.getContentAsByteArray();
        ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(bytes));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
            String fileName = zipEntry.getName();
            assertTrue(String.format("The file name %s is not expected.", fileName),
                    fileNameSet.contains(fileName));
            fileNameSet.remove(fileName);
            MultipartFile expectFile = fileNameToFileMap.get(zipEntry.getName());
            String expectContent = new String(expectFile.getBytes());
            assertEquals(expectContent, IOUtils.toString(zis, StandardCharsets.UTF_8),
                    String.format("File: <%s>'s content not matched.", fileName));
        }

        assertTrue("There are some file that are expected but not found: " +
                String.join(", ", fileNameSet), fileNameSet.isEmpty());
    }
}
