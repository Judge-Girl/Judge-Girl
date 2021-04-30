package tw.waterball.judgegirl.studentservice.domain.usecases.group;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Group;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetOwnGroupsUseCase {

    private final StudentRepository studentRepository;

    public void execute(int studentId, Presenter presenter)
            throws NotFoundException {
        Student student = studentRepository.findStudentById(studentId)
                .orElseThrow(() -> notFound(Student.class).id(studentId));
        presenter.setGroups(new ArrayList<>(student.getGroups()));
    }

    public interface Presenter {

        void setGroups(List<Group> groups);

    }


}
