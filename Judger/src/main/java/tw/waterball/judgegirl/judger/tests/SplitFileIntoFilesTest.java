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

package tw.waterball.judgegirl.judger.tests;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import tw.waterball.judgegirl.entities.problem.Problem;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import static tw.waterball.judgegirl.springboot.configs.JacksonConfig.OBJECT_MAPPER;

@SuppressWarnings("SameParameterValue")
public class SplitFileIntoFilesTest extends AbstractJudgerTest {
    @SneakyThrows
    @Override
    protected Problem getProblem() {
        var problem = OBJECT_MAPPER.readValue(
                new FileInputStream(problemHomePath + "/50179/problem.json"), Problem.class);
        problem.setDescription(IOUtils.toString(new FileInputStream(problemHomePath + "/50179/description.md"), StandardCharsets.UTF_8));
        return problem;
    }

}
