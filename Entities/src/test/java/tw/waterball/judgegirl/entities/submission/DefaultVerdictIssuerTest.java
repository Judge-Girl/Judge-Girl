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

import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultVerdictIssuerTest {
    ProgramProfile programProfile = new ProgramProfile(2000, 2000, "");

    List<Judge> judges = Arrays.asList(
            new Judge("A", JudgeStatus.WA, programProfile, 0),
            new Judge("B", JudgeStatus.WA, programProfile, 0),
            new Judge("C", JudgeStatus.WA, programProfile, 0));

    @Test
    void test() {
        VerdictIssuer issuer = new DefaultVerdictIssuer();
        issuer.setJudges(judges);

        Verdict verdict = issuer.modifyJudges(j -> j.setStatus(JudgeStatus.AC).setGrade(80))
                .addReport(new Report("TestReport"))
                .issue();

        assertNotNull(verdict.getIssueTime(), "Should have issue time.");
        verdict.getJudges()
                .forEach(j -> {
                    assertEquals(programProfile, j.getProgramProfile());
                    assertEquals(JudgeStatus.AC, j.getStatus(), "The status should have been modified.");
                    assertEquals(80, j.getGrade(), "The grade should have been modified.");
                });
        var report = verdict.getReport().getRawData();
        assertTrue(report.containsKey("TestReport"));
    }
}