package tw.waterball.judgegirl.academy.domain.usecases.homework;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.Homework;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.primitives.exam.Group;
import tw.waterball.judgegirl.primitives.exam.MemberId;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import javax.inject.Named;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.*;
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

    public GetGroupsHomeworkProgressUseCase(HomeworkRepository homeworkRepository, StudentServiceDriver studentServiceDriver, SubmissionServiceDriver submissionServiceDriver, GroupRepository groupRepository) {
        super(homeworkRepository);
        this.studentServiceDriver = studentServiceDriver;
        this.submissionServiceDriver = submissionServiceDriver;
        this.groupRepository = groupRepository;
    }

    public void execute(Request request, HomeworkProgressPresenter presenter) throws NotFoundException {
        Homework homework = findHomework(request.homeworkId);
        var groupMembers = getGroupMembers(request.groupNames);

        presenter.showProblems(homework.getProblemIds());
        presenter.showStudents(groupMembers);
        presenter.showRecords(getHomeworkRecords(homework, groupMembers));
    }

    private List<Student> getGroupMembers(List<String> groupNames) {
        var groups = groupRepository.findGroupsByNames(groupNames);
        return studentServiceDriver.getStudentsByIds(getStudentIds(groups));
    }

    private List<Integer> getStudentIds(List<Group> groups) {
        return flatMapToList(groups, group -> group.getMemberIds().stream().map(MemberId::getId).distinct());
    }

    private List<StudentSubmissionRecord> getHomeworkRecords(Homework homework, List<Student> students) {
        return mapToList(students, student -> showBestRecords(student, homework));
    }

    private StudentSubmissionRecord showBestRecords(Student student, Homework homework) {
        var bestRecords = flatMapToList(homework.getProblemIds(), problemId -> findBestRecord(student.getId(), problemId).stream());
        return new StudentSubmissionRecord(student, bestRecords);
    }

    private Optional<SubmissionView> findBestRecord(int studentId, int problemId) {
        try {
            return ofNullable(submissionServiceDriver.findBestRecord(problemId, studentId));
        } catch (NotFoundException e) {
            return empty();
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int homeworkId;
        public List<String> groupNames;
    }

}
