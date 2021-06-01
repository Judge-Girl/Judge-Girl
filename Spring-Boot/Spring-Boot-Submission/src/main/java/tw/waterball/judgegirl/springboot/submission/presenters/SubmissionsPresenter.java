package tw.waterball.judgegirl.springboot.submission.presenters;

import tw.waterball.judgegirl.primitives.submission.Submission;
import tw.waterball.judgegirl.submission.domain.usecases.GetSubmissionsUseCase;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class SubmissionsPresenter implements GetSubmissionsUseCase.Presenter {
    private List<SubmissionView> submissionViews;

    @Override
    public void showSubmissions(List<Submission> submissions) {
        this.submissionViews = mapToList(submissions, SubmissionView::toViewModel);
    }

    public List<SubmissionView> present() {
        return submissionViews;
    }
}