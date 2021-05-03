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

import tw.waterball.judgegirl.primitives.submission.report.Report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodingStyleAnalyzeReport extends Report {
    public static final String NAME = "CodingStyleAnalyzeReport";
    private final int csaScore;
    private final String formula;
    private final List<String> illegalNamingStyleList;
    private final List<String> globalVariableList;

    public CodingStyleAnalyzeReport(int csaScore, String formula, List<String> illegalNamingStyleList, List<String> globalVariableList) {
        super(NAME);
        this.csaScore = csaScore;
        this.formula = formula;
        this.illegalNamingStyleList = illegalNamingStyleList;
        this.globalVariableList = globalVariableList;
    }

    public int getCsaScore() {
        return csaScore;
    }

    public String getFormula() {
        return formula;
    }

    public List<String> getIllegalNames() {
        return illegalNamingStyleList;
    }

    public List<String> getGlobalVariables() {
        return globalVariableList;
    }


    @Override
    public Map<String, ?> getRawData() {
        var data = new HashMap<String, Object>();
        data.put("csaScore", getCsaScore());
        data.put("formula", getFormula());
        data.put("illegalNames", getIllegalNames());
        data.put("globalVariables", getGlobalVariables());
        return data;
    }
}

