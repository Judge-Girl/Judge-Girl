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

import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.problem.TestCase;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Judge {
    private TestCase testCase;
    private JudgeStatus status;
    private ProgramProfile programProfile;
    private int grade;

    public Judge(TestCase testCase,
                 JudgeStatus status,
                 ProgramProfile programProfile, int grade) {
        this.testCase = testCase;
        this.status = status;
        this.programProfile = programProfile;
        this.grade = grade;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
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
}
