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
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import java.io.FileInputStream;

import static tw.waterball.judgegirl.problemapi.views.JudgePluginTagView.toViewModel;
import static tw.waterball.judgegirl.problemapi.views.ProblemView.toEntity;
import static tw.waterball.judgegirl.springboot.configs.JacksonConfig.OBJECT_MAPPER;

@SuppressWarnings("SameParameterValue")
public class ProvidedCodesTest extends AbstractJudgerTest {
    @SneakyThrows
    @Override
    protected Problem getProblem() {
        FileInputStream fileInputStream = new FileInputStream(problemHomePath + "/70038/problem.json");
        var problem = OBJECT_MAPPER.readValue(
                fileInputStream, ProblemView.class);
        problem.setDescription("Description");
        problem.setJudgeMatchPolicyPluginTag(toViewModel(AllMatchPolicyPlugin.TAG));
        problem.getTestcases().forEach(t -> t.setId(t.getName()));
        fileInputStream.close();
        return toEntity(problem);
    }

}
