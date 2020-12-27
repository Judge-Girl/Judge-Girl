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

import tw.waterball.judgegirl.entities.submission.CodeQualityInspectionReport;
import tw.waterball.judgegirl.entities.submission.CodingStyleAnalyzeReport;
import tw.waterball.judgegirl.entities.submission.CyclomaticComplexityReport;
import tw.waterball.judgegirl.plugins.api.AbstractJudgeGirlPlugin;
import tw.waterball.judgegirl.plugins.api.codeinspection.JudgeGirlCodeQualityInspectionPlugin;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author - johnny850807@gmail.com (Waterball)
 * @author - ryan01234keroro56789@gmail.com (Giver)
 */
public abstract class AbstractJudgeGirlCodeQualityInspectionPlugin extends AbstractJudgeGirlPlugin
        implements JudgeGirlCodeQualityInspectionPlugin {
    protected Map<String, String> parameters;

    public AbstractJudgeGirlCodeQualityInspectionPlugin(Map<String, String> parameters) {
        super(parameters);
        this.parameters = parameters;
    }

    @Override
    public CodeQualityInspectionReport performAtSourceRoot(Path sourceRootPath) {
        return new CodeQualityInspectionReport(calcCyclomaticComplexity(sourceRootPath),
                                               analyzeCodingStyle(sourceRootPath.toString(), Collections.emptyList()));
    }

    protected abstract CyclomaticComplexityReport calcCyclomaticComplexity(Path sourceRootPath);

    protected abstract CodingStyleAnalyzeReport analyzeCodingStyle(String sourceRootPath, List<String> variableWhitelist);
}
