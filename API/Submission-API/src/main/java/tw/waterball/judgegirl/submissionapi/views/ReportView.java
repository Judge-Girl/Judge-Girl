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

package tw.waterball.judgegirl.submissionapi.views;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import tw.waterball.judgegirl.primitives.submission.report.Report;

import java.util.Collections;
import java.util.Map;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ToString
@EqualsAndHashCode
public class ReportView {
    private String name;
    private Map<String, ?> rawData;

    public ReportView() {
    }

    public ReportView(String name) {
        this(name, Collections.emptyMap());
    }

    public ReportView(String name, Map<String, ?> rawData) {
        this.name = name;
        this.rawData = rawData;
    }

    public static ReportView toViewModel(Report report) {
        return new ReportView(report.getName(), report.getRawData());
    }

    public static ReportView fromData(Map<String, ?> data) {
        return new ReportView("RawDataReport", data);
    }

    public String getName() {
        return name;
    }

    public Map<String, ?> getRawData() {
        return rawData;
    }

    public Report toEntity() {
        return Report.fromData(name, rawData);
    }

}
