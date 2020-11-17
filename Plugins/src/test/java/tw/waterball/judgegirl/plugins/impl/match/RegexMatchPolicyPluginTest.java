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

package tw.waterball.judgegirl.plugins.impl.match;

import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.commons.utils.ResourceUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexMatchPolicyPluginTest {
    private RegexMatchPolicyPlugin policyPlugin = new RegexMatchPolicyPlugin();

    @Test
    void testAC() {
        assertTrue(policyPlugin.isMatch(
                ResourceUtils.getAbsolutePath("/stub/test/regex-match/actual-AC-1"),
                ResourceUtils.getAbsolutePath("/stub/test/regex-match/expect"),
                Collections.emptyMap()));
        assertTrue(policyPlugin.isMatch(
                ResourceUtils.getAbsolutePath("/stub/test/regex-match/actual-AC-2"),
                ResourceUtils.getAbsolutePath("/stub/test/regex-match/expect"),
                Collections.emptyMap()));
    }

    @Test
    void testWA() {
        assertFalse(policyPlugin.isMatch(
                ResourceUtils.getAbsolutePath("/stub/test/regex-match/actual-WA-1"),
                ResourceUtils.getAbsolutePath("/stub/test/regex-match/expect"),
                Collections.emptyMap()));

        assertFalse(policyPlugin.isMatch(
                ResourceUtils.getAbsolutePath("/stub/test/regex-match/actual-WA-2"),
                ResourceUtils.getAbsolutePath("/stub/test/regex-match/expect"),
                Collections.emptyMap()));
    }
}