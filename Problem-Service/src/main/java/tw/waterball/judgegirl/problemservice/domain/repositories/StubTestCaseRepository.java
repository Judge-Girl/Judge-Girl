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

package tw.waterball.judgegirl.problemservice.domain.repositories;

import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.entities.problem.Testcase;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class StubTestCaseRepository implements TestCaseRepository {
    private final static int PROBLEM_ID = 1;
    private final static List<Testcase> STUB_TEST_CASES = Arrays.asList(
            new Testcase("1", PROBLEM_ID, 5, 5, 5000, 1, 20),
            new Testcase("2", PROBLEM_ID, 5, 5, 5000, 1, 30),
            new Testcase("3", PROBLEM_ID, 3, 4, 5000, 1, 50));

    @Override
    public List<Testcase> findAllInProblem(int problemId) {
        if (PROBLEM_ID == problemId) {
            return STUB_TEST_CASES;
        }
        return Collections.emptyList();
    }

    @Override
    public Optional<FileResource> downloadTestCaseIOs(int problemId, String testcaseIOsFileId) {
        if (PROBLEM_ID == problemId) {
            ByteArrayInputStream in = ZipUtils.zipClassPathResourcesToStream("/stubs/I1.in",
                    "/stubs/I2.in",
                    "/stubs/I3.in");

            return Optional.of(
                    new FileResource("testcases_in.zip", in.available(), in));
        }
        return Optional.empty();
    }

}
