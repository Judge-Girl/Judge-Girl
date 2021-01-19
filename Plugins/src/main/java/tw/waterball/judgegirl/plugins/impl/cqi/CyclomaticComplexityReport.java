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

import tw.waterball.judgegirl.entities.submission.Report;

import java.util.Collections;
import java.util.Map;

/**
 * @author - johnny850807@gmail.com (Waterball)
 * @author - ryan01234keroro56789@gmail.com (Giver)
 */
public class CyclomaticComplexityReport extends Report {
    public final static String NAME = "CyclomaticComplexityReport";
    private int ccScore;

    public CyclomaticComplexityReport(int ccScore) {
        super(NAME);
        this.ccScore = ccScore;
    }

    public int getScore() {
        return this.ccScore;
    }

    @Override
    public Map<String, ?> getRawData() {
        return Collections.singletonMap("ccScore", ccScore);
    }

    @Override
    public String toString() {
        return "CyclomaticComplexityReport{" +
                "ccScore=" + ccScore +
                '}';
    }
}
