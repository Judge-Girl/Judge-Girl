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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;
import tw.waterball.judgegirl.primitives.problem.validators.JudgePluginTagConstraint;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;
import static java.util.Optional.ofNullable;
import static tw.waterball.judgegirl.primitives.problem.JudgePluginTag.Type.FILTER;
import static tw.waterball.judgegirl.primitives.problem.JudgePluginTag.Type.OUTPUT_MATCH_POLICY;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Problem {
    private Integer id;
    @NotBlank
    private String title;
    @NotBlank
    private String description;  // markdown

    @Singular
    private Map<String, @Valid LanguageEnv> languageEnvs;

    @Valid
    @NotNull
    @JudgePluginTagConstraint(typeShouldBe = OUTPUT_MATCH_POLICY)
    private JudgePluginTag outputMatchPolicyPluginTag;

    @Singular
    private Set<@JudgePluginTagConstraint(typeShouldBe = {FILTER})
            JudgePluginTag> filterPluginTags;

    @Singular
    private List<@NotBlank String> tags = new ArrayList<>();

    @Singular
    private List<@Valid Testcase> testcases = new ArrayList<>();

    @NotNull
    private boolean visible;

    private String testcaseIOsFileId;

    private boolean archived;

    public void validate() {
        JSR380Utils.validate(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JudgePluginTag getOutputMatchPolicyPluginTag() {
        return outputMatchPolicyPluginTag;
    }

    public Collection<JudgePluginTag> getFilterPluginTags() {
        return filterPluginTags;
    }

    public void setFilterPluginTags(Collection<JudgePluginTag> filterPluginTags) {
        this.filterPluginTags = new HashSet<>(filterPluginTags);
    }

    public List<String> getTags() {
        return requireNonNullElse(tags, emptyList());
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getTestcaseIOsFileId() {
        return testcaseIOsFileId;
    }

    public void setTestcaseIOsFileId(String testcaseIOsFileId) {
        this.testcaseIOsFileId = testcaseIOsFileId;
    }

    public void setOutputMatchPolicyPluginTag(JudgePluginTag outputMatchPolicyPluginTag) {
        this.outputMatchPolicyPluginTag = outputMatchPolicyPluginTag;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void addLanguageEnv(LanguageEnv languageEnv) {
        if (languageEnvs == null) {
            languageEnvs = new HashMap<>();
        }
        languageEnvs.put(languageEnv.getName(), languageEnv);
    }

    public Optional<LanguageEnv> mayHaveLanguageEnv(Language language) {
        return ofNullable(getLanguageEnv(language));
    }

    public LanguageEnv getLanguageEnv(Language language) {
        if (languageEnvs == null) {
            return null;
        }
        return languageEnvs.get(language.toString());
    }

    public LanguageEnv getLanguageEnv(String name) {
        return languageEnvs.get(name);
    }

    public Map<String, LanguageEnv> getLanguageEnvs() {
        return languageEnvs;
    }

    public int getTotalGrade() {
        return testcases.stream()
                .mapToInt(Testcase::getGrade).sum();
    }

    public List<Testcase> getTestcases() {
        if (testcases == null) {
            testcases = new LinkedList<>();
        }
        return testcases;
    }
}
