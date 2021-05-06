package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.academy.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.MemberId;
import tw.waterball.judgegirl.studentapi.clients.StudentServiceDriver;

import javax.inject.Named;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Named
public class AddGroupOfExamineesUseCase extends AbstractExamUseCase {
    private final StudentServiceDriver studentServiceDriver;
    private final GroupRepository groupRepository;

    public AddGroupOfExamineesUseCase(ExamRepository examRepository, StudentServiceDriver studentServiceDriver,
                                      GroupRepository groupRepository) {
        super(examRepository);
        this.studentServiceDriver = studentServiceDriver;
        this.groupRepository = groupRepository;
    }

    public void execute(Request request) {
        Exam exam = findExam(request.examId);
        List<Integer> memberIds = getMemberIdsFromGroups(request);
        examRepository.addExaminees(exam.getId(), memberIds);
    }

    private List<Integer> getMemberIdsFromGroups(Request request) {
        return groupRepository.findGroupsByNames(request.names).stream()
                .flatMap(group -> group.getMemberIds().stream())
                .map(MemberId::getId)
                .collect(toList());
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        public int examId;
        public List<String> names;
    }
}
