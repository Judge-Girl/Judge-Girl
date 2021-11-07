package tw.waterball.judgegirl.springboot.academy.presenters;

import tw.waterball.judgegirl.academy.domain.usecases.homework.GetGroupsHomeworkProgressUseCase;
import tw.waterball.judgegirl.springboot.academy.view.StudentsHomeworkProgressView;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.List;
import java.util.Map;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.toMap;

/**
 * @author sh910913@gmail.com
 */
public class GroupsHomeworkProgressPresenter implements GetGroupsHomeworkProgressUseCase.Presenter {
    private StudentsHomeworkProgressView StudentsHomeworkProgressView = new StudentsHomeworkProgressView();

    @Override
    public void showRecords(List<GetGroupsHomeworkProgressUseCase.StudentSubmissionRecord> records) {
        StudentsHomeworkProgressView.progress = getGroupsHomeworkProgress(records);
    }

    private Map<String, StudentsHomeworkProgressView.StudentProgress> getGroupsHomeworkProgress(List<GetGroupsHomeworkProgressUseCase.StudentSubmissionRecord> records) {
        return toMap(records, record -> record.getStudent().getEmail(), this::getStudentProgress);
    }

    private StudentsHomeworkProgressView.StudentProgress getStudentProgress(GetGroupsHomeworkProgressUseCase.StudentSubmissionRecord record) {
        return new StudentsHomeworkProgressView.StudentProgress(record.getStudent().getId(), record.getStudent().getName(), getProblemScores(record));
    }

    private Map<Integer, Integer> getProblemScores(GetGroupsHomeworkProgressUseCase.StudentSubmissionRecord problemRecord) {
        return toMap(problemRecord.getRecords(), record -> record.getProblemId(), this::getProblemScore);
    }

    private Integer getProblemScore(SubmissionView record) {
        return record.getVerdict() != null ? record.getVerdict().getTotalGrade() : 0;
    }

    public StudentsHomeworkProgressView present() {
        return StudentsHomeworkProgressView;
    }

}
