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

package tw.waterball.judgegirl.entities.submission;

import lombok.*;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
public class Judge {
    private String testCaseName;
    private JudgeStatus status;
    private int runtime;
    private long memory;
    private String errorMessage;
    private int grade;

    public static Judge forCE(String testCaseName, int grade) {
        return new Judge(testCaseName, JudgeStatus.CE, 0, 0, "", grade);
    }
}
