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
import tw.waterball.judgegirl.primitives.grading.Grade;
import tw.waterball.judgegirl.primitives.grading.Grading;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.problem.Testcase;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ToString
@EqualsAndHashCode
public class Judge implements Comparable<Judge>, Grading {
    private String testcaseName;
    private JudgeStatus status;
    private ProgramProfile programProfile;
    private Grade grade;

    public Judge() {
    }

    public Judge(Testcase testcase,
                 JudgeStatus status, ProgramProfile programProfile, int grade) {
        this(testcase.getName(), status, programProfile,
                new Grade(grade, testcase.getGrade()));
    }

    public Judge(String testcaseName,
                 JudgeStatus status,
                 ProgramProfile programProfile, Grade grade) {
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

    @Override
    public int getGrade() {
        return grade.value();
    }

    public void setGrade(int grade) {
        this.grade = new Grade(grade, getMaxGrade());
    }

    @Override
    public int getMaxGrade() {
        return grade.max();
    }

    public void setMaxGrade(int maxGrade) {
        this.grade = new Grade(getGrade(), maxGrade);
    }

    @Override
    public int compareTo(@NotNull Judge judge) {
        if (getStatus() == judge.getStatus()) {
            return getProgramProfile().compareTo(judge.getProgramProfile());
        }
        return getStatus().getOrder() - judge.getStatus().getOrder();
    }

}
