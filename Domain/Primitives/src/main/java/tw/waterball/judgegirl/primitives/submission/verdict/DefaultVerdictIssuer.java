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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.report.Report;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class DefaultVerdictIssuer implements VerdictIssuer {
    private static final Logger logger = LoggerFactory.getLogger(DefaultVerdictIssuer.class);
    private final Verdict verdict;

    public DefaultVerdictIssuer(List<Judge> judges) {
        verdict = new Verdict(judges);
    }

    @Override
    public VerdictIssuer modifyJudges(Consumer<? super JudgeModifier> judgeModifierConsumer) {
        verdict.getJudges().stream()
                .map(DefaultJudgeModifier::new)
                .forEach(judgeModifierConsumer);
        return this;
    }

    @Override
    public VerdictIssuer addReport(Report report) {
        verdict.addReport(report);
        return this;
    }


    @Override
    public Verdict issue() {
        verdict.setIssueTime(new Date());
        return verdict;
    }

    public static class DefaultJudgeModifier implements JudgeModifier {
        private final Judge judge;

        public DefaultJudgeModifier(Judge judge) {
            this.judge = judge;
        }

        @Override
        public JudgeModifier setStatus(JudgeStatus status) {
            logger.info("Modifying Judge(testcase={}): Set judgeStatus: {} -> {}.",
                    judge.getTestcaseName(), judge.getStatus(), status);
            judge.setStatus(status);
            return this;
        }

        @Override
        public JudgeModifier setGrade(int grade) {
            logger.info("Modifying Judge(testcase={}): Set grade: {} -> {}.",
                    judge.getTestcaseName(), judge.getGrade(), grade);
            judge.setGrade(grade);
            return this;
        }
    }
}
