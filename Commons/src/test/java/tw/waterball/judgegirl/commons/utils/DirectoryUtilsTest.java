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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DirectoryUtilsTest {

    @Test
    public void testContentEquals() {
        // expect equal: compare two equal directories
        assertTrue(DirectoryUtils.contentEquals(
                ResourceUtils.getFile("/test/A").toPath(),
                ResourceUtils.getFile("/test/A$same").toPath()
        ));

        // expect not equal: just compare two different directories
        assertFalse(DirectoryUtils.contentEquals(
                ResourceUtils.getFile("/test/A").toPath(),
                ResourceUtils.getFile("/test/B").toPath()
        ));
        assertFalse(DirectoryUtils.contentEquals(
                ResourceUtils.getFile("/test/B").toPath(),
                ResourceUtils.getFile("/test/A").toPath()
        ));
    }

}