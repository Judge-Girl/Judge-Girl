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

package tw.waterball.judgegirl.primitives.submission.verdict;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ToString
@EqualsAndHashCode
public class ProgramProfile implements Comparable<ProgramProfile> {
    private long runtime;
    private long memoryUsage;
    private String errorMessage;

    public ProgramProfile() {
    }

    public ProgramProfile(long runtime, long memoryUsage, String errorMessage) {
        this.runtime = runtime;
        this.memoryUsage = memoryUsage;
        this.errorMessage = errorMessage;
    }

    public static ProgramProfile onlyCompileError(String compileErrorMessage) {
        return new ProgramProfile(0, 0, compileErrorMessage);
    }

    public long getRuntime() {
        return runtime;
    }

    public void setRuntime(long runtime) {
        this.runtime = runtime;
    }

    public long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean hasErrorMessage() {
        return errorMessage != null && !errorMessage.isBlank();
    }

    @Override
    public int compareTo(@NotNull ProgramProfile programProfile) {
        if (hasErrorMessage() && programProfile.hasErrorMessage()) {
            return 0;
        }
        if (hasErrorMessage()) {
            return -1;
        }
        if (programProfile.hasErrorMessage()) {
            return 1;
        }
        return (getRuntime() + getMemoryUsage()) > (programProfile.getRuntime() + programProfile.getMemoryUsage())
                ? -1 : 1;
    }
}
