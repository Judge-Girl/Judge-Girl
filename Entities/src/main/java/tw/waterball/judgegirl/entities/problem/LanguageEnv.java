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

import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * An environment for each language support.
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@Getter
@AllArgsConstructor
public class LanguageEnv {
    @NotNull
    private final Language language;

    @Valid
    private final Compilation compilation;

    @Valid
    @NotNull
    private final ResourceSpec resourceSpec;

    @NotNull
    @Singular
    private final List<@Valid SubmittedCodeSpec> submittedCodeSpecs;

    @NotBlank
    private String providedCodesFileId;

    public boolean isCompiledLanguage() {
        return language.isCompiledLanguage();
    }

    public String getName() {
        return language.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setProvidedCodesFileId(String providedCodesFileId) {
        this.providedCodesFileId = providedCodesFileId;
    }
}
