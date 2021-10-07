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
import tw.waterball.judgegirl.primitives.problem.Language;
import tw.waterball.judgegirl.primitives.problem.Problem;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.findFirst;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

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
    public List<LanguageEnvView> languageEnvs;
    public JudgePluginTagView judgeMatchPolicyPluginTag;
    public Collection<JudgePluginTagView> judgeFilterPluginTags;
    public List<String> tags;
    public List<TestcaseView> testcases;
    public int totalGrade;
    public boolean visible;
    public boolean archived;

    public Optional<LanguageEnvView> getLanguageEnv(Language language) {
        return findFirst(languageEnvs, env -> env.getLanguage() == language);
    }

    public static ProblemView toViewModel(Problem problem) {
        return new ProblemView(
                problem.getId(),
                problem.getTitle(),
                problem.getDescription(),
                mapToList(problem.getLanguageEnvs().values(), LanguageEnvView::toViewModel),
                JudgePluginTagView.toViewModel(problem.getOutputMatchPolicyPluginTag()),
                mapToList(problem.getFilterPluginTags(), JudgePluginTagView::toViewModel),
                problem.getTags(),
                mapToList(problem.getTestcases(), TestcaseView::toViewModel),
                problem.getTotalGrade(),
                problem.getVisible(),
                problem.isArchived()
        );
    }

    public static Problem toEntity(ProblemView view) {
        var builder = Problem.builder()
                .id(view.getId())
                .title(view.getTitle())
                .description(view.description)
                .outputMatchPolicyPluginTag(view.judgeMatchPolicyPluginTag.toEntity())
                .tags(requireNonNullElse(view.tags, emptyList()))
                .testcases(mapToList(view.testcases, TestcaseView::toEntity))
                .archived(view.archived);
        if (view.judgeFilterPluginTags != null) {
            builder.filterPluginTags(mapToList(view.judgeFilterPluginTags, JudgePluginTagView::toEntity));
        }
        view.languageEnvs.stream()
                .map(LanguageEnvView::toEntity)
                .forEach(languageEnv -> builder.languageEnv(languageEnv.getName(), languageEnv));
        return builder.build();
    }
}
