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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.waterball.judgegirl.cqi.csa.CodingStyleAnalyzer;
import tw.waterball.judgegirl.cqi.csa.CodingStyleAnalyzerImpl;
import tw.waterball.judgegirl.cqi.cyclomatic.CyclomaticComplexityCalculator;
import tw.waterball.judgegirl.cqi.cyclomatic.CyclomaticComplexityCalculatorImpl;
import tw.waterball.judgegirl.plugins.api.AbstractJudgeGirlPlugin;
import tw.waterball.judgegirl.plugins.api.JudgeGirlVerdictFilterPlugin;
import tw.waterball.judgegirl.plugins.api.LanguageEnvAware;
import tw.waterball.judgegirl.plugins.api.ProblemAware;
import tw.waterball.judgegirl.plugins.api.codeinspection.JudgeGirlSourceCodeFilterPlugin;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;
import tw.waterball.judgegirl.primitives.problem.LanguageEnv;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuer;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 * @author - ryan01234keroro56789@gmail.com (Giver)
 */
public class CodeQualityInspectionPlugin extends AbstractJudgeGirlPlugin
        implements ProblemAware, LanguageEnvAware, JudgeGirlSourceCodeFilterPlugin, JudgeGirlVerdictFilterPlugin {
    private static final Logger logger = LoggerFactory.getLogger(CodeQualityInspectionPlugin.class);
    public static final String GROUP = JUDGE_GIRL_GROUP;
    public static final String NAME = "CodeQualityInspection";
    public static final String DESCRIPTION = "Calculate cyclomatic complexity and perform code quality inspection" +
            "of the submission source code.";
    public static final String VERSION = "1.0";
    public static final JudgePluginTag TAG = new JudgePluginTag(JudgePluginTag.Type.FILTER, GROUP, NAME, VERSION);
    private final CyclomaticComplexityCalculator ccCalculator;
    private final CodingStyleAnalyzer csAnalyzer;
    private CodeQualityInspectionReport report;
    private Problem problem;
    private LanguageEnv languageEnv;

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
    public void onProblem(Problem problem) {
        this.problem = problem;
    }

    @Override
    public void filter(Path sourceRootPath) {
        report = new CodeQualityInspectionReport(
                calcCyclomaticComplexity(sourceRootPath),
                analyzeCodingStyle(sourceRootPath.toAbsolutePath().toString(),
                        /*TODO should have some default white list*/Collections.emptyList()));
        logger.info("Report: {}.", report);
    }

    private CyclomaticComplexityReport calcCyclomaticComplexity(Path sourceRootPath) {
        // TODO just read the src/
//        File folder = sourceRootPath.toFile();
//        File[] fileList = folder.listFiles();
//        List<String> sourceCodePaths = new ArrayList<>();
//        for (File file : requireNonNull(fileList)) {
//            if (isSubmittedCode(file.getPath())) {
//                sourceCodePaths.add(file.getPath());
//            }
//        }
//        logger.info("Inspecting the following source code: {}.", String.join(", ", sourceCodePaths));
//        List<String> sourceCodes = sourceCodePaths.stream()
//                .map(this::readSourceCode).collect(Collectors.toList());
//        var innerCcReport = ccCalculator.calculate(sourceCodes);
//        logger.info("CC-Score: {}", innerCcReport.score);
//        return new CyclomaticComplexityReport(innerCcReport.score);
        return null;
    }

//    @SneakyThrows
//    private String readSourceCode(String sourceCodePath) {
//        return Files.readString(Paths.get(sourceCodePath));
//    }


    private CodingStyleAnalyzeReport analyzeCodingStyle(String sourceRoot, List<String> variableWhitelist) {
        var report = csAnalyzer.analyze(sourceRoot, variableWhitelist);
        logger.info("CC-Score: {}", report.getScore());
        return new CodingStyleAnalyzeReport(
                report.getScore(),
                report.getFormula(),
                report.getIllegalNamingStyleList(),
                report.getGlobalVariableList());
    }

    @Override
    public void filter(VerdictIssuer verdictIssuer) {
        verdictIssuer.addReport(report);
    }

    @Override
    public void onLanguageEnv(LanguageEnv languageEnv) {
        this.languageEnv = languageEnv;
    }
}