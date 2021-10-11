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

import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.validations.ValidationUtils.validate;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Getter
public class ProvidedCodes {
    @NotBlank
    private final @Size(min = 1, max = 300) String fileId;
    @NotNull
    private final List<@Size(min = 1, max = 300) String> fileNames;

    public ProvidedCodes(String fileId, List<String> fileNames) {
        this.fileId = fileId;
        this.fileNames = fileNames;
        validate(this);
    }
}
