import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.entities.stubs.SubmissionStubBuilder.submission;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class SubmissionQueryControllerTest extends AbstractSubmissionControllerTest {

    @DisplayName("Given 3 submissions (WA, TLE, AC), When find the best record, Should respond the AC one.")
    @Test
    void testFindBestRecord() throws Exception {
        givenSubmission(submission("A").WA(30, 5));
        givenSubmission(submission("B").TLE(1000, 5));
        givenSubmission(submission("C").AC(400, 700, 100));

        SubmissionView bestRecord = getBody(mockMvc.perform(post("/api/submissions/best")
                .contentType(MediaType.TEXT_PLAIN)
                .content("A, B, C"))
                .andExpect(status().isOk()), SubmissionView.class);

        assertEquals("C", bestRecord.getId());
    }
}
