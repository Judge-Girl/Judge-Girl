package tw.waterball.judgegirl.submissionapi.views;

import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.primitives.submission.Submission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tw.waterball.judgegirl.primitives.stubs.SubmissionStubBuilder.submission;
import static tw.waterball.judgegirl.submissionapi.views.SubmissionView.toEntity;
import static tw.waterball.judgegirl.submissionapi.views.SubmissionView.toViewModel;

class SubmissionViewTest {
    @Test
    void testMapping() {
        Submission A = submission("A").CE(100).build(1, 1, "C");
        SubmissionView view = toViewModel(A);
        assertEquals(view, toViewModel(toEntity(view)));
    }
}