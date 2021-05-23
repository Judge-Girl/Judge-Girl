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

import lombok.Builder;
import lombok.Singular;
import org.jetbrains.annotations.Nullable;
import tw.waterball.judgegirl.primitives.problem.validators.JudgePluginTagConstraint;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.Optional.ofNullable;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.findFirst;
import static tw.waterball.judgegirl.commons.utils.ValidationUtils.shouldHaveLength;
import static tw.waterball.judgegirl.commons.utils.ValidationUtils.validate;
import static tw.waterball.judgegirl.primitives.problem.JudgePluginTag.Type.FILTER;
import static tw.waterball.judgegirl.primitives.problem.JudgePluginTag.Type.OUTPUT_MATCH_POLICY;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
public class Problem {
    private Integer id;

    private String title;

    @Builder.Default
    private String description = "";  // markdown

    @Singular
    private Map<String, LanguageEnv> languageEnvs;

    @JudgePluginTagConstraint(typeShouldBe = OUTPUT_MATCH_POLICY)
    private JudgePluginTag outputMatchPolicyPluginTag;

    @Singular
    private Set<@JudgePluginTagConstraint(typeShouldBe = {FILTER})
            JudgePluginTag> filterPluginTags;

    @Singular
    private List<@NotBlank String> tags;

    @Singular
    private List<@Valid Testcase> testcases;

    private boolean visible;
    private boolean archived;
    private String testcaseIOsFileId;

    public Problem(Integer id, String title, String description,
                   Map<String, LanguageEnv> languageEnvs,
                   JudgePluginTag outputMatchPolicyPluginTag,
                   Set<JudgePluginTag> filterPluginTags,
                   List<String> tags, List<Testcase> testcases, boolean visible,
                   boolean archived) {
        this(id, title, description, languageEnvs, outputMatchPolicyPluginTag,
                filterPluginTags, tags, testcases, visible, archived, null);
    }

    public Problem(Integer id, String title, String description,
                   Map<String, LanguageEnv> languageEnvs,
                   JudgePluginTag outputMatchPolicyPluginTag,
                   Set<JudgePluginTag> filterPluginTags,
                   List<String> tags, List<Testcase> testcases, boolean visible,
                   boolean archived, @Nullable String testcaseIOsFileId) {
        this.id = id;
        setTitle(title);
        setDescription(description);
        this.languageEnvs = requireNonNull(languageEnvs);
        this.outputMatchPolicyPluginTag = requireNonNull(outputMatchPolicyPluginTag);
        this.filterPluginTags = requireNonNull(filterPluginTags);
        setTags(tags);
        this.testcases = requireNonNull(testcases);
        this.visible = visible;
        this.archived = archived;
        setTestcaseIOsFileId(testcaseIOsFileId);
        validate(this);
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
        shouldHaveLength(1, 50, "title", title);
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        shouldHaveLength(0, 3000, "description", description);
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
        tags.forEach(tag -> shouldHaveLength(1, 50, "tag", tag));
        this.tags = tags;
    }

    public void addTag(String tag) {
        shouldHaveLength(1, 50, "tag", tag);
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

    public void setTestcaseIOsFileId(@Nullable String testcaseIOsFileId) {
        if (testcaseIOsFileId != null) {
            shouldHaveLength(0, 200, testcaseIOsFileId, "testcaseIOsFileId");
            this.testcaseIOsFileId = testcaseIOsFileId;
        }
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

    public void putLanguageEnv(LanguageEnv languageEnv) {
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
        return requireNonNullElse(languageEnvs, emptyMap());
    }

    public int getTotalGrade() {
        return testcases.stream()
                .mapToInt(Testcase::getGrade).sum();
    }

    public int numOfTestcases() {
        return getTestcases().size();
    }

    public void upsertTestcase(Testcase testcase) {
        getTestcaseById(testcase.getId()).ifPresent(testcases::remove);
        testcases.add(testcase);
    }

    public Optional<Testcase> getTestcaseById(String testcaseId) {
        return findFirst(testcases, testcase -> testcase.getId().equals(testcaseId));
    }

    public Testcase getTestcase(int index) {
        return getTestcases().get(index);
    }

    public List<Testcase> getTestcases() {
        if (testcases == null) {
            testcases = new LinkedList<>();
        }
        return testcases;
    }
}
