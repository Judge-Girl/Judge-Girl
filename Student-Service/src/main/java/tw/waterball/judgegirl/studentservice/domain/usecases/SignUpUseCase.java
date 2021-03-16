package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateEmailException;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class SignUpUseCase {
    private final StudentRepository studentRepository;

    public SignUpUseCase(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void execute(Request request, Presenter presenter) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Duplicate email");
        }
        Student student = new Student(request.name, request.email, request.password);
        presenter.setStudent(studentRepository.save(student));
    }

    public interface Presenter {
        void setStudent(Student student);
    }

    @Data
    @NoArgsConstructor
    public static class Request {
        @NotBlank
        public String name;
        @Email
        public String email;
        @NotBlank
        public String password;
    }
}
