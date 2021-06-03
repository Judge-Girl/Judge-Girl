package tw.waterball.judgegirl.springboot.submission.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tw.waterball.judgegirl.submission.domain.usecases.query.SubmissionQueryParams;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.List;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.primitives.stubs.SubmissionStubBuilder.submission;
import static tw.waterball.judgegirl.submission.domain.usecases.query.SortBy.ascending;
import static tw.waterball.judgegirl.submission.domain.usecases.query.SortBy.descending;
import static tw.waterball.judgegirl.submission.domain.usecases.query.SubmissionQueryParams.query;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class SubmissionQueryControllerTest extends AbstractSubmissionControllerTest {

    @DisplayName("Given 3 submissions A, B, C, When get submissions by ids A, B, D, E, should respond three submissions.")
    @Test
    void testSubmissionsByIds() {
        givenSubmission(submission("A").WA(30, 5).build(1, 1, "C"));
        givenSubmission(submission("B").TLE(1000, 5).build(2, 1, "C"));
        givenSubmission(submission("C").AC(400, 700, 100).build(3, 2, "JAVA"));

        var submissions = getSubmissionsByIds("A", "B", "D", "E");

        assertEquals(2, submissions.size());
    }

    @Test
    void testSubmissionsQuery() {
        var A = givenSubmission(submission("A").WA(30, 5).build(1, 1, "C"));
        var B = givenSubmission(submission("B").TLE(1000, 5).build(2, 1, "C"));
        var C = givenSubmission(submission("C").AC(400, 700, 100).build(1, 2, "JAVA"));

        assertEquals(2, getSubmissions(query().problemId(1).build()).size());
        assertEquals(2, getSubmissions(query().studentId(1).build()).size());
        assertEquals(1, getSubmissions(query().studentId(2).build()).size());
        assertEquals(1, getSubmissions(query().problemId(1).studentId(1).build()).size());
        assertEquals(2, getSubmissions(query().languageEnvName("C").build()).size());
        assertEquals(0, getSubmissions(query().languageEnvName("Python").build()).size());


        var actual = getSubmissions(query().sortBy(ascending("submissionTime")).build());
        assertEquals(asList(A, B, C), actual);
        actual = getSubmissions(query().sortBy(descending("submissionTime")).build());
        assertEquals(asList(C, B, A), actual);
        actual = getSubmissions(query().studentId(1).sortBy(ascending("problemId")).build());
        assertEquals(asList(A, C), actual);
    }

    @SneakyThrows
    private List<SubmissionView> getSubmissionsByIds(String... ids) {
        return getBody(mockMvc.perform(withAdminToken(get("/api/submissions")
                .queryParam("ids", join(",", ids))))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    @SneakyThrows
    private List<SubmissionView> getSubmissions(SubmissionQueryParams params) {
        var query = get("/api/submissions");
        params.getPage().ifPresent(page -> query.queryParam("page", String.valueOf(page)));
        params.getProblemId().ifPresent(problemId -> query.queryParam("problemId", String.valueOf(problemId)));
        params.getLanguageEnvName().ifPresent(langEnvName -> query.queryParam("langEnvName", langEnvName));
        params.getStudentId().ifPresent(studentId -> query.queryParam("studentId", String.valueOf(studentId)));
        params.getSortBy().ifPresent(sort -> {
            query.queryParam("sortBy", sort.getFieldName());
            query.queryParam("ascending", String.valueOf(sort.isAscending()));
        });
        return getBody(mockMvc.perform(withAdminToken(query))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    @DisplayName("Given 3 submissions (WA, TLE, AC), When find the best record, Should respond the AC one.")
    @Test
    void testFindBestRecord() throws Exception {
        givenSubmission(submission("A").WA(30, 5));
        givenSubmission(submission("B").TLE(1000, 5));
        givenSubmission(submission("C").AC(400, 700, 100));

        SubmissionView bestRecord = getBody(mockMvc.perform(withAdminToken(post("/api/submissions/best"))
                .contentType(MediaType.TEXT_PLAIN)
                .content("A, B, C"))
                .andExpect(status().isOk()), SubmissionView.class);

        assertEquals("C", bestRecord.getId());
    }
}
