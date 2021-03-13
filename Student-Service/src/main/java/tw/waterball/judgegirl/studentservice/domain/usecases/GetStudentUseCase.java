package tw.waterball.judgegirl.studentservice.domain.usecases;

import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentIdNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class GetStudentUseCase {
    private final StudentRepository studentRepository;

    public GetStudentUseCase(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    void execute(Request request, Presenter presenter) {
        presenter.setStudent(studentRepository
                .findStudentById(request.id)
                .orElseThrow(StudentIdNotFoundException::new));
    }


    public static class Request {
        private Integer id;

    }

    public interface Presenter {
        void setStudent(Student student);
    }
}