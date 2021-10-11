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
import tw.waterball.judgegirl.primitives.problem.ProvidedCodes;

import java.util.List;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProvidedCodeView {
    public String fileId;
    public List<String> fileNames;

    public static ProvidedCodeView toViewModel(ProvidedCodes providedCodes) {
        return new ProvidedCodeView(providedCodes.getFileId(), providedCodes.getFileNames());
    }

    public ProvidedCodes toValue() {
        return new ProvidedCodes(fileId, fileNames);
    }
}

