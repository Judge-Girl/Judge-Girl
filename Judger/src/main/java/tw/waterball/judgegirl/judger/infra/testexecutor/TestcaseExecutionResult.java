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

package tw.waterball.judgegirl.judger.infra.testexecutor;

import lombok.AllArgsConstructor;
import lombok.Value;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.submission.ProgramProfile;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Value
@AllArgsConstructor
public class TestcaseExecutionResult {
    private Status status;
    private ProgramProfile profile;

    public enum Status {
        SUCCESSFUL, TIME_LIMIT_EXCEEDS, MEMORY_LIMIT_EXCEEDS,
        RUNTIME_ERROR,
        OUTPUT_LIMIT_EXCEEDS, SYSTEM_ERROR;

        public JudgeStatus mapToJudgeStatus() {
            switch (this) {
                case TIME_LIMIT_EXCEEDS:
                    return JudgeStatus.TLE;
                case MEMORY_LIMIT_EXCEEDS:
                    return JudgeStatus.MLE;
                case OUTPUT_LIMIT_EXCEEDS:
                    return JudgeStatus.OLE;
                case SYSTEM_ERROR:
                    return JudgeStatus.SYSTEM_ERR;
                case RUNTIME_ERROR:
                    return JudgeStatus.RE;
                default:
                    throw new InternalError("Should not reach.");
            }
        }
    }

    public boolean isSuccessful() {
        return status == Status.SUCCESSFUL;
    }


}
