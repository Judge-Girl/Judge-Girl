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

import tw.waterball.judgegirl.entities.problem.JudgePluginTag;
import tw.waterball.judgegirl.plugins.api.ParameterMeta;
import tw.waterball.judgegirl.plugins.api.codeinspection.JudgeGirlCodeQualityInspectionPlugin;
import tw.waterball.judgegirl.entities.submission.CyclomaticComplexityReport;
import tw.waterball.judgegirl.cqi.cyclomatic.CyclomaticComplexityCalculatorImpl;
import tw.waterball.judgegirl.cqi.cyclomatic.CyclomaticComplexityCalculator;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author - johnny850807@gmail.com (Waterball)
 * @author - ryan01234keroro56789@gmail.com (Giver)
 */
public class CodeQualityInspectionCCAdapter extends AbstractJudgeGirlCodeQualityInspectionPlugin {
    public final static String GROUP = JUDGE_GIRL_GROUP;
    public final static String NAME = "CodeQualityInspection";
    public final static String DESCRIPTION = "Calculate cyclomatic complexity and perform code quality inspection" +
            "of the submission source code.";
    public final static String VERSION = "1.0";
    public final static JudgePluginTag TAG = new JudgePluginTag(JudgeGirlCodeQualityInspectionPlugin.TYPE, GROUP, NAME, VERSION);

    private CyclomaticComplexityCalculator calculator;

    public CodeQualityInspectionCCAdapter() {
        super(Collections.emptyMap());
        calculator = new CyclomaticComplexityCalculatorImpl();
    }

    @Override
    public Set<ParameterMeta> getParameterMetas() {
        return Collections.emptySet();
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
    protected CyclomaticComplexityReport calcCyclomaticComplexity(Path sourceRootPath) {
        File folder = sourceRootPath.toFile();
        File[] fileList = folder.listFiles();
        List<String> sourceCodes = new ArrayList<>();
        for(int i = 0; i < fileList.length; i++) {
            sourceCodes.add(fileList[i].getPath());
        }
        return new CyclomaticComplexityReport(calculator.calculate(sourceCodes).score);
    }
}