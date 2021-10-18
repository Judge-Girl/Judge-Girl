package tw.waterball.judgegirl.springboot.academy.presenters;

import tw.waterball.judgegirl.academy.domain.usecases.homework.GetGroupsHomeworkProgressUseCase;
import tw.waterball.judgegirl.springboot.academy.view.GroupsHomeworkProgressView;

import java.util.List;
import java.util.Map;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.toMap;

/**
 * @author sh910913@gmail.com
 */
public class GroupsHomeworkProgressPresenter implements GetGroupsHomeworkProgressUseCase.Presenter {
    private GroupsHomeworkProgressView groupsHomeworkProgressView = new GroupsHomeworkProgressView();

    @Override
    public void showRecords(List<GetGroupsHomeworkProgressUseCase.StudentSubmissionRecord> records) {
        groupsHomeworkProgressView.groupsHomeworkProgress = getGroupsHomeworkProgress(records);
    }

    private Map<String, GroupsHomeworkProgressView.StudentProgress> getGroupsHomeworkProgress(List<GetGroupsHomeworkProgressUseCase.StudentSubmissionRecord> records) {
        return toMap(records, record -> record.getStudent().getEmail(), record -> new GroupsHomeworkProgressView.StudentProgress(record.getStudent().getId(), record.getStudent().getName(), getQuestionScores(record)));
    }

    private Map<Integer, Integer> getQuestionScores(GetGroupsHomeworkProgressUseCase.StudentSubmissionRecord questionRecord) {
        return toMap(questionRecord.getRecord(), record -> record.getProblemId(), record -> record.getVerdict().getTotalGrade());
    }

    public GroupsHomeworkProgressView present() {
        return groupsHomeworkProgressView;
    }

}
