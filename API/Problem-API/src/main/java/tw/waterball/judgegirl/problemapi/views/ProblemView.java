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

package tw.waterball.judgegirl.problemapi.views;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.problem.JudgePluginTag;
import tw.waterball.judgegirl.entities.problem.LanguageEnv;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.Testcase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemView {
    public Integer id;
    public String title;
    public String description;
    public List<LanguageEnv> languageEnvs;
    public JudgePluginTag judgeMatchPolicyPluginTag;
    public Collection<JudgePluginTag> judgeFilterPluginTags;
    public List<String> tags;
    public String testcaseIOsFileId;
    public List<Testcase> testcases;
    public boolean visible;

    public static ProblemView fromEntity(Problem problem) {
        return new ProblemView(
                problem.getId(),
                problem.getTitle(),
                problem.getDescription(),
                new ArrayList<>(problem.getLanguageEnvs().values()),
                problem.getOutputMatchPolicyPluginTag(),
                problem.getFilterPluginTags(),
                problem.getTags(),
                problem.getTestcaseIOsFileId(),
                problem.getTestcases(),
                problem.getVisible()
        );
    }

    public static Problem toEntity(ProblemView view) {
        var builder = Problem.builder()
                .id(view.getId())
                .title(view.getTitle())
                .description(view.description)
                .outputMatchPolicyPluginTag(view.judgeMatchPolicyPluginTag)
                .tags(view.tags)
                .testcases(view.testcases)
                .testcaseIOsFileId(view.testcaseIOsFileId);
        for (LanguageEnv languageEnv : view.languageEnvs) {
            builder.languageEnv(languageEnv.getName(), languageEnv);
        }
        if (view.judgeFilterPluginTags != null) {
            builder.filterPluginTags(view.judgeFilterPluginTags);
        }
        return builder.build();
    }
}
