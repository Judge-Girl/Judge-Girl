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

package tw.waterball.judgegirl.plugins.impl.cqi;

import tw.waterball.judgegirl.cqi.codingStyle.CodingStyleAnalyzer;
import tw.waterball.judgegirl.cqi.codingStyle.CodingStyleAnalyzerImpl;
import tw.waterball.judgegirl.cqi.cyclomatic.CyclomaticComplexityCalculator;
import tw.waterball.judgegirl.cqi.cyclomatic.CyclomaticComplexityCalculatorImpl;
import tw.waterball.judgegirl.entities.problem.JudgePluginTag;
import tw.waterball.judgegirl.entities.submission.*;
import tw.waterball.judgegirl.plugins.api.AbstractJudgeGirlPlugin;
import tw.waterball.judgegirl.plugins.api.JudgeGirlVerdictFilterPlugin;
import tw.waterball.judgegirl.plugins.api.codeinspection.JudgeGirlSourceCodeFilterPlugin;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author - johnny850807@gmail.com (Waterball)
 * @author - ryan01234keroro56789@gmail.com (Giver)
 */
public class CodeQualityInspectionPlugin extends AbstractJudgeGirlPlugin
        implements JudgeGirlSourceCodeFilterPlugin, JudgeGirlVerdictFilterPlugin {
    public final static String GROUP = JUDGE_GIRL_GROUP;
    public final static String NAME = "CodeQualityInspection";
    public final static String DESCRIPTION = "Calculate cyclomatic complexity and perform code quality inspection" +
            "of the submission source code.";
    public final static String VERSION = "1.0";
    public final static JudgePluginTag TAG = new JudgePluginTag(JudgePluginTag.Type.FILTER, GROUP, NAME, VERSION);

    private CyclomaticComplexityCalculator ccCalculator;
    private CodingStyleAnalyzer csAnalyzer;
    private CodeQualityInspectionReport report;

    public CodeQualityInspectionPlugin() {
        ccCalculator = new CyclomaticComplexityCalculatorImpl();
        csAnalyzer = new CodingStyleAnalyzerImpl();
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public JudgePluginTag getTag() {
        return TAG;
    }

    @Override
    public void filter(Path sourceRootPath) {
        report = new CodeQualityInspectionReport(calcCyclomaticComplexity(sourceRootPath),
                analyzeCodingStyle(sourceRootPath.toString(), Collections.emptyList()));
    }

    private CyclomaticComplexityReport calcCyclomaticComplexity(Path sourceRootPath) {
        File folder = sourceRootPath.toFile();
        File[] fileList = folder.listFiles();
        List<String> sourceCodes = new ArrayList<>();
        for (File file : requireNonNull(fileList)) {
            sourceCodes.add(file.getPath());
        }
        return new CyclomaticComplexityReport(ccCalculator.calculate(sourceCodes).score);
    }

    private CodingStyleAnalyzeReport analyzeCodingStyle(String sourceRoot, List<String> variableWhitelist) {
        var report = csAnalyzer.analyze(sourceRoot, variableWhitelist);
        return new CodingStyleAnalyzeReport(report.rawString);
    }

    @Override
    public void filter(VerdictIssuer verdictIssuer) {
        verdictIssuer.addReport(report);
    }
}