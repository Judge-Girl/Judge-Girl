package tw.waterball.judgegirl.submissionapi.clients;

import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.primitives.submission.Bag;
import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.primitives.submission.SubmissionThrottlingException;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.commons.utils.ArrayUtils.contains;
import static tw.waterball.judgegirl.submissionapi.views.SubmissionView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class FakeSubmissionServiceDriver implements SubmissionServiceDriver {
    private final Map<String, Submission> submissions = new HashMap<>();

    @Override
    public SubmissionView submit(SubmitCodeRequest request) throws SubmissionThrottlingException {
        String id = String.valueOf(submissions.size() + 1);
        var submission = new Submission(id, request.studentId,
                request.problemId, request.languageEnvName, randomUUID().toString());
        submissions.put(id, submission);
        return toViewModel(submission);
    }

    @Override
    public SubmissionView getSubmission(int problemId, int studentId, String submissionId) throws NotFoundException {
        var submission = submissions.get(submissionId);
        if (submission == null ||
                submission.getProblemId() == problemId && submission.getStudentId() == studentId) {
            throw notFound(Submission.class).id(submissionId);
        }
        return toViewModel(submission);
    }

    @Override
    public FileResource downloadSubmittedCodes(int problemId, int studentId, String submissionId, String submittedCodesFileId) throws NotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<SubmissionView> getSubmissions(int problemId, int studentId, Map<String, String> bagQueryParameters) {
        return submissions.values().stream()
                .filter(submission -> submission.getProblemId() == problemId &&
                        submission.getStudentId() == studentId && filter(submission, bagQueryParameters))
                .map(SubmissionView::toViewModel)
                .collect(toList());
    }

    private boolean filter(Submission submission, Map<String, String> bagQueryParameters) {
        Bag bag = submission.getBag();
        for (var entry : bagQueryParameters.entrySet()) {
            String key = entry.getKey();
            if (!bag.get(key).equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public List<SubmissionView> getSubmissions(String... submissionIds) {
        return submissions.values().stream()
                .filter(submission -> contains(submissionIds, submission.getId()))
                .map(SubmissionView::toViewModel).collect(toList());
    }

    @Override
    public SubmissionView findBestRecord(List<String> submissionIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SubmissionView findBestRecord(int problemId, int studentId) throws NotFoundException {
        throw new UnsupportedOperationException();
    }

    public void add(Submission submission) {
        submissions.put(submission.getId(), submission);
    }
}
