package tw.waterball.judgegirl.springboot.academy.presenters;

import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.academy.domain.usecases.homework.GetStudentsHomeworkProgressUseCase;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.springboot.academy.view.StudentsHomeworkProgress;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.List;

import static java.util.function.Function.identity;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.toMap;

/**
 * @author sh910913@gmail.com
 */
public class StudentsHomeworkProgressPresenter implements GetStudentsHomeworkProgressUseCase.Presenter {

    private List<Integer> problemIds;
    private List<Student> students;
    private List<GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord> records;

    @Override
    public void showProblems(List<Integer> problemIds) {
        this.problemIds = problemIds;
    }

    @Override
    public void showStudents(List<Student> students) {
        this.students = students;
    }

    @Override
    public void showRecords(List<GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord> records) {
        this.records = records;
    }

    public StudentsHomeworkProgress present() {
        StudentsHomeworkProgress studentsHomeworkProgress = new StudentsHomeworkProgress();
        var studentRecords = toMap(records, GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord::getStudentId, this::getStudentProgress);
        students.forEach(student -> studentsHomeworkProgress.addProgress(student.getEmail(), studentRecords.get(student.getId())));
        return studentsHomeworkProgress;
    }

    @NotNull
    private StudentsHomeworkProgress.StudentProgress getStudentProgress(GetStudentsHomeworkProgressUseCase.StudentSubmissionRecord record) {
        Student student = record.getStudent();
        var studentProgress = new StudentsHomeworkProgress.StudentProgress(student.getId(), student.getName());
        var problemRecords = toMap(record.getRecords(), SubmissionView::getProblemId, identity());
        problemIds.forEach(problemId -> studentProgress.addProblemScore(problemId, getProblemScore(problemRecords.get(problemId))));
        return studentProgress;
    }

    private int getProblemScore(SubmissionView record) {
        return record == null ? 0 : record.getVerdict().getTotalGrade();
    }
}
