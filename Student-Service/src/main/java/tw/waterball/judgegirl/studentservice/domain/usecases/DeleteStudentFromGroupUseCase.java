package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.studentservice.domain.repositories.GroupRepository;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class DeleteStudentFromGroupUseCase {

    private final StudentRepository studentRepository;

    private final GroupRepository groupRepository;

    public void execute(int groupId, int studentId)
            throws NotFoundException {
        Group group = groupRepository.findGroupById(groupId)
                .orElseThrow(NotFoundException::new);
        group.deleteStudentById(studentId);
        groupRepository.save(group);
    }
}
