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

package tw.waterball.judgegirl.springboot.configs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tw.waterball.judgegirl.entities.submission.CompositeReport;
import tw.waterball.judgegirl.entities.submission.Report;
import tw.waterball.judgegirl.submissionapi.views.ReportView;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = JacksonConfig.class)
class JacksonConfigTest {
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testDeserializingReportView() throws JsonProcessingException {
        CompositeReport compositeReport = new CompositeReport();
        compositeReport.addReport(new Report("A"));
        compositeReport.addReport(new Report("B"));
        compositeReport.addReport(new Report("C", () -> singletonMap("C-1", 1)));

        ReportView reportView = ReportView.fromEntity(compositeReport);
        String json = objectMapper.writeValueAsString(reportView);
        ReportView actualReportView = objectMapper.readValue(json, ReportView.class);
        assertEquals(reportView, actualReportView);
    }
}