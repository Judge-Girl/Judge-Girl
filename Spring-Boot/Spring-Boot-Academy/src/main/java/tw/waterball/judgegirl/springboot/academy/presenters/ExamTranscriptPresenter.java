package tw.waterball.judgegirl.springboot.academy.presenters;

import tw.waterball.judgegirl.academy.domain.usecases.exam.CalculateExamScoreUseCase;
import tw.waterball.judgegirl.academy.domain.usecases.exam.CalculateExamScoreUseCase.ExamineeRecord;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.springboot.academy.view.TranscriptView;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ExamTranscriptPresenter implements CalculateExamScoreUseCase.Presenter {
    private final TranscriptView.TranscriptViewBuilder builder = TranscriptView.builder();

    @Override
    public void showExam(Exam exam) {
    }

    @Override
    public void showEveryRecord(ExamineeRecord examineeRecord) {
        builder.examineeRecord(examineeRecord.getExaminee().getEmail(),
                new TranscriptView.ExamineeRecord(
                        examineeRecord.getTotalScore(), examineeRecord.getScores()
                ));
    }

    @Override
    public void showStatistics(double averageScore, int maxScore) {
        builder.averageScore(averageScore)
                .maxScore(maxScore);
    }

    public TranscriptView present() {
        return builder.build();
    }
}
