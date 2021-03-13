package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;


/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class SignUpUseCase {
    private StudentRepository studentRepository;

    public SignUpUseCase(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void execute(Request request, Presenter presenter) {
        Student student = new Student(request.name, request.email, request.password);
        presenter.setStudent(studentRepository.save(student));
    }

    public interface Presenter {
        void setStudent(Student student);
    }

    @Data
    @NoArgsConstructor
    public static class Request {
        public String name, email, password;
    }
}
