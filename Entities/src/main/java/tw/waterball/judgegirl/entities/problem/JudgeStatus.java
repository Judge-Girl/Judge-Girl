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

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public enum JudgeStatus {
    /**
     * All Correct
     */
    AC("All Correct"),

    /**
     * Time Limit Error
     */
    TLE("Time Limit Error"),

    /**
     * Memory Limit Error
     */
    MLE("Memory Limit Error"),

    /**
     * Wrong Answer
     */
    WA("Wong Answer"),


    /**
     * Compile Error
     */
    CE("Compile Error"),

    /**
     * Output Limit Error
     */
    OLE("Output Limit Error"),

    /**
     * Runtime Error
     */
    RE("Runtime Error"),

    /**
     * Presentation Error
     */
    PE("Presentation Error"),

    /**
     * Null Object
     */
    NONE("None"),

    /**
     * System error indicates that the judge cannot complete due to the system error
     */
    SYSTEM_ERR("System Error");

    private String fullName;

    JudgeStatus(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }

}
