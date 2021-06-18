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

package tw.waterball.judgegirl.plugins.impl.match;

import tw.waterball.judgegirl.plugins.api.AbstractJudgeGirlPlugin;
import tw.waterball.judgegirl.plugins.api.exceptions.MatchPolicyPluginException;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;

import java.nio.file.Path;
import java.util.Map;

/**
 * Abstract policy plugin that contains many template method hooks.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class AbstractJudgeGirlMatchPolicyPlugin extends AbstractJudgeGirlPlugin
        implements JudgeGirlMatchPolicyPlugin {

    public AbstractJudgeGirlMatchPolicyPlugin(Map<String, String> parameters) {
        super(parameters);
    }

    @Override
    public boolean isMatch(Path actualStandardOutputPath,
                           Path expectStandardOutputPath,
                           Map<Path, Path> actualToExpectOutputFilePathMap) throws MatchPolicyPluginException {
        try {
            if (onDetermineTwoFileContentMatches(actualStandardOutputPath, expectStandardOutputPath)) {
                for (Map.Entry<Path, Path> pair : actualToExpectOutputFilePathMap.entrySet()) {
                    if (!onDetermineTwoFileContentMatches(pair.getKey(), pair.getValue())) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        } catch (Exception err) {
            throw new MatchPolicyPluginException(err);
        }
    }


    protected abstract boolean onDetermineTwoFileContentMatches(Path actualFilePath, Path expectFilePath) throws Exception;

}
