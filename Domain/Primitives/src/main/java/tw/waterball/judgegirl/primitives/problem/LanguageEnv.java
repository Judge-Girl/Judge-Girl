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
import lombok.Getter;
import lombok.Singular;
import org.jetbrains.annotations.Nullable;

import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNullElseGet;
import static java.util.Optional.ofNullable;
import static tw.waterball.judgegirl.commons.utils.validations.ValidationUtils.validate;

/**
 * An environment for each language support.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@Getter
public class LanguageEnv {
    @NotNull
    private final Language language;

    @NotNull
    private Compilation compilation;

    @NotNull
    private ResourceSpec resourceSpec;

    @NotNull
    @Singular
    private List<SubmittedCodeSpec> submittedCodeSpecs;

    @Nullable
    private ProvidedCodes providedCodes;

    public LanguageEnv(Language language,
                       Compilation compilation,
                       ResourceSpec resourceSpec,
                       List<SubmittedCodeSpec> submittedCodeSpecs) {
        this(language, compilation, resourceSpec, submittedCodeSpecs, null);
    }

    public LanguageEnv(Language language,
                       Compilation compilation,
                       ResourceSpec resourceSpec,
                       List<SubmittedCodeSpec> submittedCodeSpecs,
                       @Nullable ProvidedCodes providedCodes) {
        this.language = language;
        this.compilation = compilation;
        this.resourceSpec = resourceSpec;
        this.submittedCodeSpecs = submittedCodeSpecs;
        this.providedCodes = providedCodes;
        validate(this);
    }

    public boolean isCompiledLanguage() {
        return language.isCompiledLanguage();
    }

    public String getName() {
        return language.toString();
    }

    public void setResourceSpec(ResourceSpec resourceSpec) {
        this.resourceSpec = resourceSpec;
    }

    public void setCompilationScript(String compilationScript) {
        this.compilation = new Compilation(compilationScript);
    }

    public List<SubmittedCodeSpec> getSubmittedCodeSpecs() {
        return submittedCodeSpecs = requireNonNullElseGet(submittedCodeSpecs, LinkedList::new);
    }

    public Optional<ProvidedCodes> getProvidedCodes() {
        return ofNullable(providedCodes);
    }

    public void setProvidedCodes(@Nullable ProvidedCodes providedCodes) {
        this.providedCodes = providedCodes;
    }

    public Optional<String> getProvidedCodesFileId() {
        return getProvidedCodes().map(ProvidedCodes::getProvidedCodesFileId);
    }

    @Override
    public String toString() {
        return getName();
    }
}
