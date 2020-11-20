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

package tw.waterball.judgegirl.judger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tw.waterball.judgegirl.judgerapi.env.JudgerEnvVariables;

import java.io.IOException;

/**
 * The CCJudger's bootstrap.
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Main {
    private final static Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws IOException {
        logger.info("Running CCJudger...");
        JudgerEnvVariables.Values values = JudgerEnvVariables.fromSystemEnvs();
        System.out.println(values);

        CCJudger judger = DefaultCCJudgerFactory.create(values,
                "/judger-layout.yaml");
        logger.info("CCJudger has been instantiated.");
        judger.judge(values.studentId, values.problemId, values.submissionId);
        logger.info("CCJudger has completed the judge.");
    }

}
