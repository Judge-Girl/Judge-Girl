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

import tw.waterball.judgegirl.entities.problem.JudgePluginTag;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPluginLocator;
import tw.waterball.judgegirl.entities.submission.CodeQualityInspectionReport;
import tw.waterball.judgegirl.plugins.api.codeinspection.JudgeGirlCodeQualityInspectionPlugin;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;

import java.nio.file.Path;
import java.util.Map;

/**
 * Abstract Judger that encapsulates the plugin-extensions.
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("WeakerAccess")
public abstract class PluginExtendedJudger extends Judger {
    private JudgeGirlPluginLocator pluginLocator;

    public PluginExtendedJudger(JudgeGirlPluginLocator pluginLocator) {
        this.pluginLocator = pluginLocator;
    }

    @Override
    protected boolean isProgramOutputAllCorrect(Testcase testcase) {
        JudgeGirlMatchPolicyPlugin matchPolicyPlugin =
                locateMatchPolicyPlugin(getProblem().getOutputMatchPolicyPluginTag());
        return matchPolicyPlugin.isMatch(
                getActualStandardOutputPath(testcase),
                getExpectStandardOutputPath(testcase),
                getActualToExpectOutputFilePathMap(testcase)
        );
    }

    private JudgeGirlMatchPolicyPlugin locateMatchPolicyPlugin(JudgePluginTag tag) {
        return (JudgeGirlMatchPolicyPlugin) pluginLocator.locate(tag);
    }

    protected abstract Path getActualStandardOutputPath(Testcase testcase);

    protected abstract Path getExpectStandardOutputPath(Testcase testcase);

    /**
     * @return a mapping between the paths of every pair of the 'actual file-output' and the 'expected file-output'
     */
    protected abstract Map<Path, Path> getActualToExpectOutputFilePathMap(Testcase testcase);

    @Override
    protected CodeQualityInspectionReport doCodeInspection(JudgePluginTag codeInspectionTag) {
        JudgeGirlCodeQualityInspectionPlugin codeInspectionPlugin = locateCodeInspectionPlugin(codeInspectionTag);
        return doCodeInspection(codeInspectionPlugin);
    }

    private JudgeGirlCodeQualityInspectionPlugin locateCodeInspectionPlugin(JudgePluginTag tag) {
        return (JudgeGirlCodeQualityInspectionPlugin) pluginLocator.locate(tag);
    }

    private CodeQualityInspectionReport doCodeInspection(JudgeGirlCodeQualityInspectionPlugin codeInspectionPlugin) {
        return codeInspectionPlugin.performAtSourceRoot(getCodeInspectionHomePath());
    }

    protected abstract Path getCodeInspectionHomePath();

}