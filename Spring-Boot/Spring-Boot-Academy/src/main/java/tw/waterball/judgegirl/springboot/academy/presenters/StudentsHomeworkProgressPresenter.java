package tw.waterball.judgegirl.springboot.academy.presenters;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.toMap;

import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.academy.domain.usecases.homework.GetStudentsHomeworkProgressUseCase;
import tw.waterball.judgegirl.springboot.academy.view.StudentsHomeworkProgressView;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.List;
import java.util.Map;

/**
 * @author sh910913@gmail.com
 */
public class StudentsHomeworkProgressPresenter implements GetStudentsHomeworkProgressUseCase.Presenter {
    private StudentsHomeworkProgressView studentsHomeworkProgressView = new StudentsHomeworkProgressView();

    @Override
    public void showRecords(List<GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord> records) {
        studentsHomeworkProgressView.progress = getScoreBoard(records);
    }

    private Map<String, StudentsHomeworkProgressView.StudentProgress> getScoreBoard(List<GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord> records) {
        return toMap(records, record -> record.getStudent().getEmail(), this::getStudentProgress);
    }

    @NotNull
    private StudentsHomeworkProgressView.StudentProgress getStudentProgress(GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord record) {
        return new StudentsHomeworkProgressView.StudentProgress(record.getStudent().getId(), record.getStudent().getName(), getProblemScores(record));
    }

    private Map<Integer, Integer> getProblemScores(GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord problemRecord) {
        return toMap(problemRecord.getRecords(), record -> record.getProblemId(), this::getProblemScore);
    }

    private Integer getProblemScore(SubmissionView record) {
        return record.getVerdict() != null ? record.getVerdict().getTotalGrade() : 0;
    }

    public StudentsHomeworkProgressView present() {
        return studentsHomeworkProgressView;
    }

}
