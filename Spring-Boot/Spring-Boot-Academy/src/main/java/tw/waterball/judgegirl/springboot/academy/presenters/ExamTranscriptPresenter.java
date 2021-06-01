package tw.waterball.judgegirl.springboot.academy.presenters;

import tw.waterball.judgegirl.academy.domain.usecases.exam.CalculateExamScoreUseCase;
import tw.waterball.judgegirl.academy.domain.usecases.exam.CalculateExamScoreUseCase.ExamineeRecord;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.springboot.academy.view.TranscriptView;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toUnmodifiableList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.toMap;
import static tw.waterball.judgegirl.submissionapi.views.SubmissionView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ExamTranscriptPresenter implements CalculateExamScoreUseCase.Presenter {
    private final TranscriptView.TranscriptViewBuilder builder = TranscriptView.builder();

    @Override
    public void showExam(Exam exam) {
    }

    @Override
    public void showRecords(List<ExamineeRecord> examineeRecords) {
        Map<String, TranscriptView.ExamineeRecordView> stringExamineeRecordMap = mapEmailToExamineeRecord(examineeRecords);
        builder.scoreBoard(stringExamineeRecordMap);
        builder.records(examineeRecords.stream()
                .flatMap(r -> r.getQuestionRecords().stream())
                .map(questionRecord -> new TranscriptView.RecordView(
                        questionRecord.getQuestion().getExamId(),
                        questionRecord.getQuestion().getProblemId(),
                        toViewModel(questionRecord.getRecord())
                )).collect(toUnmodifiableList()));
    }

    private Map<String, TranscriptView.ExamineeRecordView> mapEmailToExamineeRecord(List<ExamineeRecord> examineeRecords) {
        return toMap(examineeRecords, examineeRecord -> examineeRecord.getExaminee().getEmail(),
                this::presentExamineeRecord);
    }

    private TranscriptView.ExamineeRecordView presentExamineeRecord(ExamineeRecord examineeRecord) {
        var builder = TranscriptView.ExamineeRecordView.builder();
        examineeRecord.getQuestionRecords()
                .forEach(questionRecord -> builder.questionScore(
                        questionRecord.getQuestion().getProblemId(),
                        questionRecord.calculateScore()));
        return builder.build();
    }

    public TranscriptView present() {
        return builder.build();
    }


}
