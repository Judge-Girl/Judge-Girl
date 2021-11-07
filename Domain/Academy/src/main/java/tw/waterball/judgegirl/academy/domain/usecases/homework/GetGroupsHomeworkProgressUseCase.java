package tw.waterball.judgegirl.academy.domain.usecases.homework;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.Homework;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Group;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import javax.inject.Named;
import java.util.*;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.flatMapToList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - sh910913@gmail.com(gordon.liao)
 */
@Named
public class GetGroupsHomeworkProgressUseCase extends AbstractHomeworkUseCase {

    private final StudentServiceDriver studentServiceDriver;
    private final SubmissionServiceDriver submissionServiceDriver;
    private final GroupRepository groupRepository;

    public GetGroupsHomeworkProgressUseCase(HomeworkRepository homeworkRepository, StudentServiceDriver studentServiceDriver, GroupRepository groupRepository, SubmissionServiceDriver submissionServiceDriver) {
        super(homeworkRepository);
        this.studentServiceDriver = studentServiceDriver;
        this.submissionServiceDriver = submissionServiceDriver;
        this.groupRepository = groupRepository;
    }

    public void execute(Request request, Presenter presenter) throws NotFoundException {
        Homework homework = findHomework(request.homeworkId);
        var students = getGroupMembers(request.groupNames);
        presenter.showRecords(getQuestionRecords(homework, students));
    }

    private List<Student> getGroupMembers(List<String> groupNames) {
        var groups = groupRepository.findGroupsByNames(groupNames);
        return findStudents(getStudentIds(groups));
    }

    private List<Student> findStudents(List<Integer> studentIds) {
        return studentServiceDriver.getStudentsByIds(studentIds);
    }

    private List<Integer> getStudentIds(List<Group> groups) {
        return flatMapToList(groups, group -> group.getMemberIds().stream().map(m -> m.getId()).distinct());
    }

    private List<StudentSubmissionRecord> getQuestionRecords(Homework homework, List<Student> students) {
        return mapToList(students, student -> showBestRecords(student, homework));
    }


    private StudentSubmissionRecord showBestRecords(Student student, Homework homework) {
        var bestRecords = flatMapToList(homework.getProblemIds(), problemId -> findBestRecord(student.getId(), problemId).stream());
        return new StudentSubmissionRecord(student, bestRecords);
    }

    private Optional<SubmissionView> findBestRecord(int studentId, int problemId) {
        try {
            return of(submissionServiceDriver.findBestRecord(problemId, studentId));
        } catch (NotFoundException e) {
            return of(new SubmissionView(null, studentId, problemId, null, null, null, null));
        }
    }

    public interface Presenter {

        void showRecords(List<StudentSubmissionRecord> records);

    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int homeworkId;
        public List<String> groupNames;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentSubmissionRecord {
        public Student student;
        public List<SubmissionView> records;

    }
}
