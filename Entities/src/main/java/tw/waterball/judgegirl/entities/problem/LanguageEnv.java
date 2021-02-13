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
@Setter
@AllArgsConstructor
public class LanguageEnv {
    @NotBlank
    private String name;

    @Valid
    private Compilation compilation;

    @Valid
    @NotNull
    private ResourceSpec resourceSpec;

    @NotNull
    @Singular
    private List<@Valid SubmittedCodeSpec> submittedCodeSpecs;

    @NotBlank
    private String providedCodesFileId;

}
