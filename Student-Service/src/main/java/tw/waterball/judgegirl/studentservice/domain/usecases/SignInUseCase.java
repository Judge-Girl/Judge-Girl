package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentEmailNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentPasswordIncorrectException;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class SignInUseCase {
    private final StudentRepository studentRepository;

    public SignInUseCase(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void execute(Request request, Presenter presenter)
            throws StudentEmailNotFoundException, StudentPasswordIncorrectException {

        Student student = studentRepository
                .findByEmail(request.email)
                .orElseThrow(StudentEmailNotFoundException::new);
        passwordChecking(student.getPassword(), request.password);
        presenter.setStudent(student);

    }

    private void passwordChecking(String studentPwd, String requestPwd) throws StudentPasswordIncorrectException {
        if (!studentPwd.equals(requestPwd)) {
            throw new StudentPasswordIncorrectException();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public String email, password;
    }

    public interface Presenter {
        void setStudent(Student student);
    }
}
