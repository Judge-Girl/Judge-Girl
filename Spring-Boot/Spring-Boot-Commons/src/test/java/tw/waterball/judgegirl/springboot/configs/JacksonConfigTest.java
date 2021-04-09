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
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.entities.submission.report.CompositeReport;
import tw.waterball.judgegirl.entities.submission.report.Report;
import tw.waterball.judgegirl.entities.submission.verdict.Judge;
import tw.waterball.judgegirl.entities.submission.verdict.ProgramProfile;
import tw.waterball.judgegirl.entities.submission.verdict.Verdict;
import tw.waterball.judgegirl.springboot.ScanRoot;
import tw.waterball.judgegirl.submissionapi.views.ReportView;

import java.util.Arrays;
import java.util.Date;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tw.waterball.judgegirl.submissionapi.views.VerdictView.toViewModel;

@SpringBootTest(classes = ScanRoot.class)
class JacksonConfigTest {
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void testDeserializingReportView() throws JsonProcessingException {
        CompositeReport compositeReport = new CompositeReport();
        compositeReport.addReport(new Report("A"));
        compositeReport.addReport(new Report("B"));
        compositeReport.addReport(new Report("C", () -> singletonMap("C-1", 1)));

        ReportView reportView = ReportView.toViewModel(compositeReport);
        String json = objectMapper.writeValueAsString(reportView);
        ReportView actualReportView = objectMapper.readValue(json, ReportView.class);
        assertEquals(reportView, actualReportView);
    }

    @Test
    void testDeserializingVerdict() throws JsonProcessingException {
        Verdict verdict = new Verdict(
                Arrays.asList(new Judge("T", JudgeStatus.AC, new ProgramProfile(10, 10, ""), 50),
                        new Judge("T", JudgeStatus.WA, new ProgramProfile(10, 100, ""), 50)
                ), new Date());

        String json = objectMapper.writeValueAsString(verdict);
        Verdict actualVerdict = objectMapper.readValue(json, Verdict.class);

        assertEquals(toViewModel(verdict), toViewModel(actualVerdict));
    }
}