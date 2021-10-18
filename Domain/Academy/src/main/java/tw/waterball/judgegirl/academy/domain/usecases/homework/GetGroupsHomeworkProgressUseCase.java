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

    public void execute(Request request, Presenter presenter)
            throws NotFoundException {
        Homework homework = findHomework(request.homeworkId);

        var students = getGroupMembers(request.groupNames);
        presenter.showRecords(getQuestionRecords(homework, students));
    }

    private List<Student> getGroupMembers(List<String> groupNames) {
        var groups = groupRepository.findGroupsByNames(groupNames);
        var students = findStudents(getStudentIds(groups));
        return students;
    }

    private List<StudentSubmissionRecord> getQuestionRecords(Homework homework, List<Student> students) {
        return mapToList(students, student -> showBestRecords(student, homework));
    }

    private List<Integer> getStudentIds(List<Group> groups) {
        return flatMapToList(groups, group -> group.getMemberIds().stream().map(m -> m.getId()).distinct());
    }

    private List<Student> findStudents(List<Integer> studentIds) {
        return studentServiceDriver.getStudentsByIds(studentIds);
    }


    private StudentSubmissionRecord showBestRecords(Student student, Homework homework) {
        var submissionViews = flatMapToList(homework.getProblemIds(), problemId -> findBestRecord(student.getId(), problemId).stream());
        return new StudentSubmissionRecord(student, submissionViews);
    }

    private Optional<SubmissionView> findBestRecord(int studentId, int problemId) {
        try {
            SubmissionView bestRecord = submissionServiceDriver.findBestRecord(problemId, studentId);
            return bestRecord != null ? Optional.of(submissionServiceDriver.findBestRecord(problemId, studentId)) : Optional.empty();
        } catch (NotFoundException e) {
            return Optional.empty();
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
        public List<SubmissionView> record;

    }
}
