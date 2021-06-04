package tw.waterball.judgegirl.springboot.configs.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.primitives.grading.Grade;
import tw.waterball.judgegirl.primitives.problem.JudgeStatus;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.events.VerdictIssuedEvent;
import tw.waterball.judgegirl.primitives.submission.report.CompositeReport;
import tw.waterball.judgegirl.primitives.submission.report.Report;
import tw.waterball.judgegirl.primitives.submission.verdict.Judge;
import tw.waterball.judgegirl.primitives.submission.verdict.ProgramProfile;
import tw.waterball.judgegirl.primitives.submission.verdict.Verdict;
import tw.waterball.judgegirl.springboot.configs.JacksonConfig;
import tw.waterball.judgegirl.submissionapi.views.ReportView;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tw.waterball.judgegirl.primitives.time.DateProvider.now;
import static tw.waterball.judgegirl.submissionapi.views.VerdictView.toViewModel;

class VerdictIssuedEventJacksonConfigTest {
    static final ObjectMapper objectMapper = JacksonConfig.OBJECT_MAPPER;

    @Test
    void testCEVerdictIssuedEvent() throws JsonProcessingException {
        testVerdictIssuedEventUsingObjectMapper(
                Verdict.compileError("", 100));
    }

    @Test
    void testWAVerdictIssuedEvent() throws JsonProcessingException {
        testVerdictIssuedEventUsingObjectMapper(new Verdict(
                asList(new Judge("T", JudgeStatus.AC, new ProgramProfile(10, 10, ""),
                                new Grade(50, 50)),
                        new Judge("T", JudgeStatus.WA, new ProgramProfile(10, 100, ""),
                                new Grade(0, 50))
                ), now()));
    }

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

    private void testVerdictIssuedEventUsingObjectMapper(Verdict verdict) throws JsonProcessingException {
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