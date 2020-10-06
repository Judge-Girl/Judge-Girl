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

import java.util.ArrayList;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */

@Builder
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class Problem {
    private Integer id;
    private String title;
    private String markdownDescription;
    private JudgeSpec judgeSpec;
    private JudgePluginTag judgePolicyPluginTag;

    @Singular
    private List<String> inputFileNames;

    @Singular
    private List<String> outputFileNames;

    @Singular
    private List<String> tags = new ArrayList<>();

    @Singular
    private List<SubmittedCodeSpec> submittedCodeSpecs = new ArrayList<>();

    private Compilation compilation;

    private String providedCodesFileId;
    private String testcaseIOsFileId;

    //    TODO @JsonIgnore
    public String getProvidedCodesFileName() {
        return String.format("%d_%s_provided.zip", id, title);
    }

    //    TODO @JsonIgnore
    public String getTestCaseIOsFileName() {
        return String.format("%d_%s_IO.zip", id, title);
    }
}
