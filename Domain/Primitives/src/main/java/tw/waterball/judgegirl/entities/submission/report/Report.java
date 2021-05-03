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

package tw.waterball.judgegirl.entities.submission.report;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Report {
    public static final Report EMPTY = new Report("Empty");
    private final String name;
    private final Supplier<Map<String, ?>> rawDataSupplier;
    
    public static RawDataReport fromData(String name, Map<String, ?> data) {
        return new RawDataReport(name, data);
    }

    public Report(String name) {
        this(name, Collections::emptyMap);
    }

    public Report(String name, Map<String, ?> rawData) {
        this(name, () -> rawData);
    }

    public Report(String name, Supplier<Map<String, ?>> rawDataSupplier) {
        this.name = name;
        this.rawDataSupplier = rawDataSupplier;
    }

    public String getName() {
        return name;
    }

    public Map<String, ?> getRawData() {
        return rawDataSupplier.get();
    }

    public static class RawDataReport extends Report {
        private final Map<String, ?> data;

        public RawDataReport(String name, Map<String, ?> data) {
            super(name, () -> data);
            this.data = data;
        }

        public Object get(String key) {
            return data.get(key);
        }
    }
}
