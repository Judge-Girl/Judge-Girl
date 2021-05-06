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

package tw.waterball.judgegirl.plugins.api.match;

import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPlugin;
import tw.waterball.judgegirl.plugins.api.exceptions.MatchPolicyPluginException;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface JudgeGirlMatchPolicyPlugin extends JudgeGirlPlugin {
    JudgePluginTag.Type TYPE = JudgePluginTag.Type.OUTPUT_MATCH_POLICY;

    /**
     * Judge for the status.
     *
     * @param actualStandardOutputPath        the file name of whose stores the actual standard output
     * @param expectStandardOutputPath        the file name of whose stores the expected standard output
     * @param actualToExpectOutputFilePathMap the mapping each entry maps the file name of whose stores one of the
     *                                        actual output to the file name of whose stores one of the
     * @return true if all actual-to-expect output files matched according to the policy
     */
    boolean isMatch(Path actualStandardOutputPath, Path expectStandardOutputPath,
                    Map<Path, Path> actualToExpectOutputFilePathMap) throws MatchPolicyPluginException;

}
