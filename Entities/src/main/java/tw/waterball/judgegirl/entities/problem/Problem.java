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

package tw.waterball.judgegirl.entities.problem;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;
import tw.waterball.judgegirl.entities.problem.validators.JudgePluginTagConstraint;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.entities.problem.JudgePluginTag.Type.*;

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
    private String markdownDescription;

    @Valid
    @NotNull
    private JudgeEnvSpec judgeEnvSpec;

    @Valid
    @NotNull
    @JudgePluginTagConstraint(typeShouldBe = OUTPUT_MATCH_POLICY)
    private JudgePluginTag outputMatchPolicyPluginTag;

    @Singular
    private Set<@JudgePluginTagConstraint(typeShouldBe = {SOURCE_CODE_FILTER, VERDICT_FILTER})
            JudgePluginTag> filterPluginTags;

    @Singular
    private List<@NotBlank String> inputFileNames;
    @Singular
    private List<@NotBlank String> outputFileNames;
    @Singular
    private List<@NotBlank String> tags;
    @Singular
    private List<@Valid SubmittedCodeSpec> submittedCodeSpecs;

    @Valid
    private Compilation compilation;
    private String providedCodesFileId;
    private String testcaseIOsFileId;

    public void validate() {
        JSR380Utils.validate(this);
    }

    public String getProvidedCodesFileName() {
        return String.format("%d-%s-provided.zip", id, title);
    }

    public String getTestCaseIOsFileName() {
        return String.format("%d-%s-IO.zip", id, title);
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

    public String getMarkdownDescription() {
        return markdownDescription;
    }

    public void setMarkdownDescription(String markdownDescription) {
        this.markdownDescription = markdownDescription;
    }

    public JudgeEnvSpec getJudgeEnvSpec() {
        return judgeEnvSpec;
    }

    public void setJudgeEnvSpec(JudgeEnvSpec judgeEnvSpec) {
        this.judgeEnvSpec = judgeEnvSpec;
    }

    public JudgePluginTag getOutputMatchPolicyPluginTag() {
        return outputMatchPolicyPluginTag;
    }

    public Collection<JudgePluginTag> getFilterPluginTagsOfType(JudgePluginTag.Type type) {
        return filterPluginTags.stream()
                .filter(tag -> tag.getType() == type)
                .collect(Collectors.toSet());
    }

    public Collection<JudgePluginTag> getFilterPluginTags() {
        return filterPluginTags;
    }

    public List<String> getInputFileNames() {
        return inputFileNames;
    }

    public void setInputFileNames(List<String> inputFileNames) {
        this.inputFileNames = inputFileNames;
    }

    public List<String> getOutputFileNames() {
        return outputFileNames;
    }

    public void setOutputFileNames(List<String> outputFileNames) {
        this.outputFileNames = outputFileNames;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public List<SubmittedCodeSpec> getSubmittedCodeSpecs() {
        return submittedCodeSpecs;
    }

    public void setSubmittedCodeSpecs(List<SubmittedCodeSpec> submittedCodeSpecs) {
        this.submittedCodeSpecs = submittedCodeSpecs;
    }

    public Compilation getCompilation() {
        return compilation;
    }

    public void setCompilation(Compilation compilation) {
        this.compilation = compilation;
    }

    public String getProvidedCodesFileId() {
        return providedCodesFileId;
    }

    public void setProvidedCodesFileId(String providedCodesFileId) {
        this.providedCodesFileId = providedCodesFileId;
    }

    public String getTestcaseIOsFileId() {
        return testcaseIOsFileId;
    }

    public void setTestcaseIOsFileId(String testcaseIOsFileId) {
        this.testcaseIOsFileId = testcaseIOsFileId;
    }

    public boolean isCompiledLanguage() {
        return getJudgeEnvSpec().getLanguage().isCompiledLang();
    }

    public void setOutputMatchPolicyPluginTag(JudgePluginTag outputMatchPolicyPluginTag) {
        this.outputMatchPolicyPluginTag = outputMatchPolicyPluginTag;
    }
}
