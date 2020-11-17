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
import tw.waterball.judgegirl.entities.submission.CodeInspectionReport;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPluginLocator;
import tw.waterball.judgegirl.plugins.api.codeinspection.JudgeGirlCodeInspectionPlugin;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;

import java.nio.file.Path;
import java.util.Map;

/**
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

    protected abstract Map<Path, Path> getActualToExpectOutputFilePathMap(Testcase testcase);

    @Override
    protected CodeInspectionReport doCodeInspection(JudgePluginTag codeInspectionTag) {
        JudgeGirlCodeInspectionPlugin codeInspectionPlugin = locateCodeInspectionPlugin(codeInspectionTag);
        return doCodeInspection(codeInspectionPlugin);
    }

    private JudgeGirlCodeInspectionPlugin locateCodeInspectionPlugin(JudgePluginTag tag) {
        return (JudgeGirlCodeInspectionPlugin) pluginLocator.locate(tag);
    }

    private CodeInspectionReport doCodeInspection(JudgeGirlCodeInspectionPlugin codeInspectionPlugin) {
        return codeInspectionPlugin.doCodeInspection(getCodeInspectionHomePath());
    }

    protected abstract Path getCodeInspectionHomePath();

}
