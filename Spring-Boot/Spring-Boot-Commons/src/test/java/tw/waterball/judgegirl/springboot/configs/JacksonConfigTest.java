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
import tw.waterball.judgegirl.primitives.grading.Grade;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.report.CompositeReport;
import tw.waterball.judgegirl.primitives.submission.report.Report;
import tw.waterball.judgegirl.primitives.submission.verdict.Judge;
import tw.waterball.judgegirl.primitives.submission.verdict.ProgramProfile;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuedEvent;
import tw.waterball.judgegirl.springboot.ScanRoot;
import tw.waterball.judgegirl.submissionapi.views.ReportView;

import java.util.Arrays;
import java.util.Date;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tw.waterball.judgegirl.primitives.time.DateProvider.now;
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
    void testDeserializingVerdictIssuedEvent() throws JsonProcessingException {
        Verdict verdict = new Verdict(
                Arrays.asList(new Judge("T", JudgeStatus.AC, new ProgramProfile(10, 10, ""),
                                new Grade(50, 50)),
                        new Judge("T", JudgeStatus.WA, new ProgramProfile(10, 100, ""),
                                new Grade(0, 50))
                ), new Date());
        var event = new VerdictIssuedEvent(1, "p", 10, "s", verdict,
                now(), new Bag("k-1", "v-l").addEntry("k-2", "v-2").addEntry("k-3", "v-3"));

        String json = objectMapper.writeValueAsString(event);
        var actualEvent = objectMapper.readValue(json, VerdictIssuedEvent.class);

        // First compare the equality of the Verdict and then compare the whole event
        assertEquals(toViewModel(event.getVerdict()), toViewModel(actualEvent.getVerdict()));
        event.setVerdict(actualEvent.getVerdict());
        assertEquals(event, actualEvent);
    }
}