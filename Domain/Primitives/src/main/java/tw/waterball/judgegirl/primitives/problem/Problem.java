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
import tw.waterball.judgegirl.primitives.problem.validators.JudgePluginTagConstraint;

import javax.validation.constraints.NotBlank;
import java.util.*;

import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.Optional.ofNullable;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.findFirst;
import static tw.waterball.judgegirl.commons.utils.validations.ValidationUtils.shouldHaveLength;
import static tw.waterball.judgegirl.commons.utils.validations.ValidationUtils.validate;
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
    private List<Testcase> testcases;

    private boolean visible;
    private boolean archived;

    public Problem(Integer id, String title, String description,
                   Map<String, LanguageEnv> languageEnvs,
                   JudgePluginTag outputMatchPolicyPluginTag,
                   Set<JudgePluginTag> filterPluginTags,
                   List<String> tags, List<Testcase> testcases, boolean visible,
                   boolean archived) {
        this.id = id;
        setTitle(title);
        setDescription(description);
        this.languageEnvs = new HashMap<>(languageEnvs);
        this.outputMatchPolicyPluginTag = requireNonNull(outputMatchPolicyPluginTag);
        this.filterPluginTags = new HashSet<>(filterPluginTags);
        setTags(new ArrayList<>(tags));
        this.testcases = new ArrayList<>(testcases);
        this.visible = visible;
        this.archived = archived;
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
        filterPluginTags = requireNonNullElseGet(filterPluginTags, HashSet::new);
        return unmodifiableSet(filterPluginTags);
    }

    public void setFilterPluginTags(Collection<JudgePluginTag> filterPluginTags) {
        this.filterPluginTags = new HashSet<>(filterPluginTags);
    }

    public List<String> getTags() {
        return tags = requireNonNullElseGet(tags, LinkedList::new);
    }

    public void setTags(List<String> tags) {
        tags.forEach(tag -> shouldHaveLength(1, 50, "tag", tag));
        this.tags = tags;
    }

    public void addTag(String tag) {
        shouldHaveLength(1, 50, "tag", tag);
        getTags().add(tag);
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
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
        languageEnvs.put(languageEnv.getName(), languageEnv);
    }

    public Optional<LanguageEnv> mayHaveLanguageEnv(Language language) {
        return ofNullable(getLanguageEnv(language));
    }

    public LanguageEnv getLanguageEnv(Language language) {
        return getLanguageEnv(language.toString());
    }

    public LanguageEnv getLanguageEnv(String name) {
        return getLanguageEnvs().get(name);
    }

    public Map<String, LanguageEnv> getLanguageEnvs() {
        languageEnvs = requireNonNullElseGet(languageEnvs, HashMap::new);
        return unmodifiableMap(languageEnvs);
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
        if (testcases.stream().noneMatch(t -> t.getName().equals(testcase.getName()))) {
            testcases.add(testcase);
        } else {
            throw new IllegalStateException("Duplicate testcase's name");
        }
    }

    public Optional<Testcase> getTestcaseById(String testcaseId) {
        return findFirst(testcases, testcase -> testcase.getId().equals(testcaseId));
    }

    public Testcase getTestcase(int index) {
        return getTestcases().get(index);
    }

    public List<Testcase> getTestcases() {
        testcases = requireNonNullElseGet(testcases, LinkedList::new);
        return unmodifiableList(testcases);
    }
}
