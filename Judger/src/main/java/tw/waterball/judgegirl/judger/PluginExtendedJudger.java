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
import tw.waterball.judgegirl.entities.problem.JudgePluginTag;
import tw.waterball.judgegirl.entities.problem.Problem;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.entities.submission.VerdictIssuer;
import tw.waterball.judgegirl.plugins.api.*;
import tw.waterball.judgegirl.plugins.api.codeinspection.JudgeGirlSourceCodeFilterPlugin;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

/**
 * Abstract Judger that encapsulates the plugin-extensions.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
@SuppressWarnings("WeakerAccess")
public abstract class PluginExtendedJudger extends Judger {
    private final static Logger logger = LogManager.getLogger(PluginExtendedJudger.class);
    private JudgeGirlPluginLocator pluginLocator;

    public PluginExtendedJudger(JudgeGirlPluginLocator pluginLocator) {
        this.pluginLocator = pluginLocator;
    }

    @Override
    protected void onJudgeContextSetup(JudgeContext judgeContext) {
        Problem problem = judgeContext.getProblem();
        var plugins = new ArrayList<JudgePluginTag>();
        plugins.add(problem.getOutputMatchPolicyPluginTag());
        plugins.addAll(problem.getFilterPluginTags());
        for (JudgePluginTag tag : plugins) {
            JudgeGirlPlugin plugin = pluginLocator.locate(tag);
            if (plugin instanceof ProblemAware) {
                ((ProblemAware) plugin).setProblem(problem);
            }
            if (plugin instanceof TestcasesAware) {
                ((TestcasesAware) plugin).setTestcases(judgeContext.testcases);
            }
            if (plugin instanceof SubmissionAware) {
                ((SubmissionAware) plugin).setSubmission(judgeContext.submission);
            }
        }
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
    protected void doSourceCodeFilteringForTag(JudgePluginTag tag) {
        logger.info("doSourceCodeFilteringForTag: {}.", tag);
        try {
            JudgeGirlSourceCodeFilterPlugin plugin = (JudgeGirlSourceCodeFilterPlugin) pluginLocator.locate(tag);
            plugin.filter(getSourceRootPath());
        } catch (Exception err) {
            logger.error("Error occurs on source code filtering for the tag {}", tag, err);
        }
    }

    @Override
    protected void doVerdictFilteringForTag(VerdictIssuer verdictIssuer, JudgePluginTag tag) {
        logger.info("doVerdictFilteringForTag: {}.", tag);
        try {
            JudgeGirlVerdictFilterPlugin plugin = (JudgeGirlVerdictFilterPlugin) pluginLocator.locate(tag);
            plugin.filter(verdictIssuer);
        } catch (Exception err) {
            logger.error("Error occurs on verdict filtering for the tag {}", tag, err);
        }
    }

    protected abstract Path getSourceRootPath();

}
