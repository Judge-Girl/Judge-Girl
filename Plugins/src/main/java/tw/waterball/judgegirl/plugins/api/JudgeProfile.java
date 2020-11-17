/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.plugins.api;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class JudgeProfile {
    private boolean compileError;
    private int runtime;
    private long memory;
    private int exitCode;
    private String errorMessage;

    public JudgeProfile(boolean compileError, int runtime, long memory, int exitCode, String errorMessage) {
        this.compileError = compileError;
        this.runtime = runtime;
        this.memory = memory;
        this.exitCode = exitCode;
        this.errorMessage = errorMessage;
    }

    public boolean isCompileError() {
        return compileError;
    }

    public void setCompileError(boolean compileError) {
        this.compileError = compileError;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public long getMemory() {
        return memory;
    }

    public void setMemory(int memory) {
        this.memory = memory;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
