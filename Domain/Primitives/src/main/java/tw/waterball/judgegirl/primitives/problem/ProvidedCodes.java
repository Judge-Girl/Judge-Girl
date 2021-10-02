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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Getter
public class ProvidedCodes {
    @NotNull
    private final String providedCodesFileId;
    @NotNull
    private final List<@Size(min = 1, max = 300) String> providedCodesFileName;

    public ProvidedCodes(String providedCodesFileId, List<String> providedCodesFileName) {
        validateProvidedCodesFileId(providedCodesFileId);
        this.providedCodesFileId = providedCodesFileId;
        this.providedCodesFileName = providedCodesFileName;
    }

    private void validateProvidedCodesFileId(String providedCodesFileId) {
        if (providedCodesFileId != null &&
                (providedCodesFileId.isEmpty() ||
                        providedCodesFileId.length() > 300)) {
            throw new IllegalStateException("The providedCodesFileId's length must be > 0 and <= 300");
        }
    }
}
