package tw.waterball.judgegirl.springboot.academy.presenters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.usecases.exam.AnswerQuestionUseCase;
import tw.waterball.judgegirl.primitives.exam.Answer;
import tw.waterball.judgegirl.springboot.academy.view.AnswerView;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import static tw.waterball.judgegirl.springboot.academy.view.AnswerView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class AnswerQuestionPresenter implements AnswerQuestionUseCase.Presenter {
    private Answer answer;
    private SubmissionView submissionView;
    private int remainingSubmissionQuota;

    @Override
    public void showRemainingSubmissionQuota(int remainingSubmissionQuota) {
        this.remainingSubmissionQuota = remainingSubmissionQuota;
    }

    @Override
    public void showAnswer(Answer answer, SubmissionView submissionView) {
        this.answer = answer;
        this.submissionView = submissionView;
    }

    public AnswerQuestionPresenter.View present() {
        return new AnswerQuestionPresenter.View(remainingSubmissionQuota, toViewModel(answer), submissionView);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class View {
        private int remainingSubmissionQuota;
        private AnswerView answer;
        private SubmissionView submission;
    }
}
