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

package tw.waterball.judgegirl.springboot.submission.impl.mongo.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerdictData {
    private List<JudgeData> judges = new ArrayList<>();
    private Date issueTime;
    private int totalGrade;
    private int maxGrade;
    private JudgeStatus summaryStatus;
    private String compileErrorMessage;
    private String systemErrorMessage;
    private String reportName;
    private Map<String, ?> reportData;
}
