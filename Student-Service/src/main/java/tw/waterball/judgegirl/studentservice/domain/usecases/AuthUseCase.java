package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.token.TokenInvalidException;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class AuthUseCase {
    private final StudentRepository studentRepository;

    public AuthUseCase(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void execute(Request request, Presenter presenter) throws TokenInvalidException {
        presenter.setStudent(studentRepository
                .findStudentById(request.id)
                .orElseThrow(TokenInvalidException::new));
    }

    @Value
    public static class Request {
        public Integer id;
    }

    public interface Presenter {
        void setStudent(Student student);

        void setToken(TokenService.Token token);
    }
}
