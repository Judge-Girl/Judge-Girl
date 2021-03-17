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

/**
 * The tests for a Judger.
 * <p>
 * There are reasons that we can't put these test classes into 'src/main/test':
 * - We put the test classes in the main source so that maven will
 * package them into the judger's jar, which will be run in the container
 * with being tested by the JUnit5 engine.
 */
package tw.waterball.judgegirl.judger.tests;