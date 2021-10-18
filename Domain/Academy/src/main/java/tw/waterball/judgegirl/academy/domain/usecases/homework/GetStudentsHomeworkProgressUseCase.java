package tw.waterball.judgegirl.academy.domain.usecases.homework;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.Homework;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import javax.inject.Named;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.flatMapToList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - sh910913@gmail.com(gordon.liao)
 */
@Named
public class GetStudentsHomeworkProgressUseCase extends AbstractHomeworkUseCase {

    private final StudentServiceDriver studentServiceDriver;
    private final SubmissionServiceDriver submissionServiceDriver;

    public GetStudentsHomeworkProgressUseCase(HomeworkRepository homeworkRepository, StudentServiceDriver studentServiceDriver, SubmissionServiceDriver submissionServiceDriver) {
        super(homeworkRepository);
        this.studentServiceDriver = studentServiceDriver;
        this.submissionServiceDriver = submissionServiceDriver;
    }

    public void execute(Request request, Presenter presenter)
            throws NotFoundException {
        Homework homework = findHomework(request.homeworkId);
        var students = findStudents(request.emails);
        presenter.showRecords(getStudentSubmissionRecord(homework, students));
    }

    private List<StudentSubmissionRecord> getStudentSubmissionRecord(Homework homework, List<Student> students) {
        return mapToList(students, student -> showBestRecords(student, homework));
    }

    private List<Student> findStudents(List<String> emails) {
        return studentServiceDriver.getStudentsByEmails(emails);
    }

    private StudentSubmissionRecord showBestRecords(Student student, Homework homework) {
        var submissionViews = flatMapToList(homework.getProblemIds(), problemId -> findBestRecord(student.getId(), problemId).stream());
        return new StudentSubmissionRecord(student, submissionViews);
    }

    private Optional<SubmissionView> findBestRecord(int studentId, int problemId) {
        try {
            return of(submissionServiceDriver.findBestRecord(problemId, studentId));
        } catch (NotFoundException e) {
            return empty();
        }
    }

    public interface Presenter {

        void showRecords(List<StudentSubmissionRecord> records);

    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int homeworkId;
        public List<String> emails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSubmissionRecord {
        Student student;
        List<SubmissionView> record;

    }
}
