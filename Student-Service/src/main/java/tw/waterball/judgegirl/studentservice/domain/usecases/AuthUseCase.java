package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentIdNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class AuthUseCase {
    private final StudentRepository studentRepository;
    private final TokenService tokenService;

    public AuthUseCase(StudentRepository studentRepository, TokenService tokenService) {
        this.studentRepository = studentRepository;
        this.tokenService = tokenService;
    }

    public void execute(Request request, Presenter presenter) {
        TokenService.Token token = tokenService.parseAndValidate(request.tokenString);
        presenter.setToken(tokenService.renewToken(token.getToken()));
        presenter.setEmail(studentRepository
                .findStudentById(token.getStudentId())
                .orElseThrow(StudentIdNotFoundException::new)
                .getEmail());
    }

    @Value
    public static class Request {
        public String tokenString;
    }

    public interface Presenter {
        void setEmail(String email);
        void setToken(TokenService.Token token);
    }


}
