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

package tw.waterball.judgegirl.primitives.submission.report;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class CompositeReport extends Report {
    private final Collection<Report> reports;

    public CompositeReport() {
        this(new HashSet<>());
    }

    public CompositeReport(Collection<Report> reports) {
        this("CompositeReport", reports);
    }

    public CompositeReport(String name, Collection<Report> reports) {
        super(name);
        this.reports = reports;
    }

    public void addReport(Report report) {
        reports.add(report);
    }

    public void removeReport(Report report) {
        reports.remove(report);
    }

    @Override
    public Map<String, Map<String, ?>> getRawData() {
        return reports.stream()
                .collect(Collectors.toMap(Report::getName, Report::getRawData));
    }
}
