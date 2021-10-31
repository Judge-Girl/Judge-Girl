package tw.waterball.judgegirl.springboot.academy.presenters;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.toMap;

import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.academy.domain.usecases.homework.GetStudentsHomeworkProgressUseCase;
import tw.waterball.judgegirl.springboot.academy.view.StudentsHomeworkProgressView;

import java.util.List;
import java.util.Map;

/**
 * @author sh910913@gmail.com
 */
public class StudentsHomeworkProgressPresenter implements GetStudentsHomeworkProgressUseCase.Presenter {
    private StudentsHomeworkProgressView studentsHomeworkProgressView = new StudentsHomeworkProgressView();

    @Override
    public void showRecords(List<GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord> records) {
        studentsHomeworkProgressView.scoreBoard = getScoreBoard(records);
    }

    private Map<String, StudentsHomeworkProgressView.StudentProgress> getScoreBoard(List<GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord> records) {
        return toMap(records, record -> record.getStudent().getEmail(), this::getStudentProgress);
    }

    @NotNull
    private StudentsHomeworkProgressView.StudentProgress getStudentProgress(GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord record) {
        return new StudentsHomeworkProgressView.StudentProgress(record.getStudent().getId(),record.getStudent().getName(),getQuestionScores(record));
    }

    private Map<Integer, Integer> getQuestionScores(GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord questionRecord) {
        return toMap(questionRecord.getRecords(), record -> record.getProblemId(), record -> record.getVerdict().getTotalGrade());
    }

    public StudentsHomeworkProgressView present() {
        return studentsHomeworkProgressView;
    }

}
