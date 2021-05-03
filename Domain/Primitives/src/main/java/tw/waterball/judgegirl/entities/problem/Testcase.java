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
import tw.waterball.judgegirl.commons.utils.JSR380Utils;
import tw.waterball.judgegirl.entities.problem.validators.PositiveOrNegativeOne;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.PositiveOrZero;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Testcase {
    @NotBlank
    private String name;
    @PositiveOrZero
    private int problemId;
    @PositiveOrNegativeOne
    private int timeLimit;
    @PositiveOrNegativeOne
    private long memoryLimit;
    @PositiveOrNegativeOne
    private long outputLimit;
    @PositiveOrNegativeOne
    private int threadNumberLimit;
    @PositiveOrZero
    private int grade;

    public Testcase(int problemId, int timeLimit, long memoryLimit, long outputLimit, int threadNumberLimit, int grade) {
        this.problemId = problemId;
        this.timeLimit = timeLimit;
        this.memoryLimit = memoryLimit;
        this.outputLimit = outputLimit;
        this.threadNumberLimit = threadNumberLimit;
        this.grade = grade;
    }

    public void validate() {
        JSR380Utils.validate(this);
    }
}
