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
import lombok.ToString;
import tw.waterball.judgegirl.commons.utils.validations.EnglishLetterOrDigitOrDash;
import tw.waterball.judgegirl.primitives.problem.validators.PositiveOrNegativeOne;

import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static tw.waterball.judgegirl.commons.utils.validations.ValidationUtils.validate;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Getter
@ToString
public class Testcase {

    @Size(min = 1, max = 200)
    @EnglishLetterOrDigitOrDash
    private final String id;
    @Size(min = 1, max = 50)
    @EnglishLetterOrDigitOrDash
    private final String name;
    @PositiveOrZero
    private final int problemId;
    @PositiveOrNegativeOne
    private final int timeLimit;
    @PositiveOrNegativeOne
    private final long memoryLimit;
    @PositiveOrNegativeOne
    private final long outputLimit;
    @PositiveOrNegativeOne
    private final int threadNumberLimit;
    @PositiveOrZero
    private final int grade;
    private TestcaseIO testcaseIO;

    public Testcase(String name, int problemId, int timeLimit, long memoryLimit,
                    long outputLimit, int threadNumberLimit, int grade) {
        this(name, name, problemId, timeLimit, memoryLimit, outputLimit, threadNumberLimit, grade, null);
    }


    public Testcase(String id, String name, int problemId, int timeLimit, long memoryLimit,
                    long outputLimit, int threadNumberLimit, int grade) {
        this(id, name, problemId, timeLimit, memoryLimit, outputLimit, threadNumberLimit, grade, null);
    }

    public Testcase(String id, String name, int problemId, int timeLimit, long memoryLimit,
                    long outputLimit, int threadNumberLimit, int grade, TestcaseIO testcaseIO) {
        this.id = id;
        this.name = name;
        this.problemId = problemId;
        this.timeLimit = timeLimit;
        this.memoryLimit = memoryLimit;
        this.outputLimit = outputLimit;
        this.threadNumberLimit = threadNumberLimit;
        this.grade = grade;
        this.testcaseIO = testcaseIO;
        validate(this);
    }

    public Optional<TestcaseIO> getTestcaseIO() {
        return ofNullable(testcaseIO);
    }

    public void setTestcaseIO(TestcaseIO testcaseIO) {
        this.testcaseIO = testcaseIO;
    }
}
