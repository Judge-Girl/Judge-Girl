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

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ToString
@EqualsAndHashCode
public class Judge implements Comparable<Judge> {
    private String testcaseName;
    private JudgeStatus status;
    private ProgramProfile programProfile;
    private int grade;

    public Judge() {
    }

    public Judge(String testcaseName,
                 JudgeStatus status,
                 ProgramProfile programProfile, int grade) {
        this.testcaseName = testcaseName;
        this.status = status;
        this.programProfile = programProfile;
        this.grade = grade;
    }

    public String getTestcaseName() {
        return testcaseName;
    }

    public void setTestcase(String testcaseName) {
        this.testcaseName = testcaseName;
    }

    public JudgeStatus getStatus() {
        return status;
    }

    public void setStatus(JudgeStatus status) {
        this.status = status;
    }

    public ProgramProfile getProgramProfile() {
        return programProfile;
    }

    public void setProgramProfile(ProgramProfile programProfile) {
        this.programProfile = programProfile;
    }

    public int getGrade() {
        return grade;
    }

    public void setGrade(int grade) {
        this.grade = grade;
    }

    @Override
    public int compareTo(@NotNull Judge judge) {
        if (getStatus() == judge.getStatus()) {
            return getProgramProfile().compareTo(judge.getProgramProfile());
        }
        return getJudgeStatusOder(getStatus()) - getJudgeStatusOder(judge.getStatus());
    }

    private int getJudgeStatusOder(JudgeStatus judgeStatus) {
        switch (judgeStatus) {
            case AC:
                return Integer.MAX_VALUE; // the best
            case WA:
            case RE:
            case MLE:
            case TLE:
            case OLE:
            case PE:
                return 300;
            case CE:
                return 200;
            case SYSTEM_ERR:
                return 0;
            case NONE:
                return -1;
            default:
                throw new IllegalStateException("enums has not been totally covered.");
        }
    }

}
