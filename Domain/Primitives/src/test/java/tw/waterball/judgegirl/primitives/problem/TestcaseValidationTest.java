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

package tw.waterball.judgegirl.primitives.problem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TestcaseValidationTest {

    @Test
    public void testShouldBeValid() {
        new Testcase("name", 0, 3, 3, 3, -1, 50);
    }

    @Test
    public void givenZeroTimeLimit_ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new Testcase("name", 0, 0, 3, 3, -1, 50));
    }

    @Test
    public void givenZeroMemoryLimit_ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new Testcase("name", 0, 3, 0, 3, -1, 50));
    }

    @Test
    public void givenZeroOutputLimit_ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new Testcase("name", 0, -1, 3, 0, -1, 50));
    }

    @Test
    public void givenZeroThreadLimit_ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new Testcase("name", 0, 3, -1, 3, 0, 50));
    }

    @Test
    public void givenNegativeGrade_ShouldBeInvalid() {
        assertThrows(RuntimeException.class,
                () -> new Testcase("name", 0, 3, -1, 3, 3, -1));
    }

}