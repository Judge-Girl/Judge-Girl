package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.exceptions.AccountNotFoundException;
import tw.waterball.judgegirl.springboot.student.exceptions.PasswordIncorrectException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentEmailNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentPasswordIncorrectException;
import tw.waterball.judgegirl.studentservice.domain.usecases.GetStudentUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignInUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignUpUseCase;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@RequestMapping("/api/students")
@RestController
@AllArgsConstructor
public class StudentController {
    private final SignInUseCase signInUseCase;
    private final SignUpUseCase signUpUseCase;
    private final TokenService tokenService;

    @PostMapping("/signUp")
    public Student signUp(@RequestBody SignUpUseCase.Request request) {
        SignUpPresenter presenter = new SignUpPresenter();
        signUpUseCase.execute(request, presenter);
        return presenter.present();
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody SignInUseCase.Request request) {
        SignInPresenter presenter = new SignInPresenter(tokenService);
        try {
            signInUseCase.execute(request, presenter);
            return presenter.present();
        } catch (StudentEmailNotFoundException e) {
            throw new AccountNotFoundException(e);
        } catch (StudentPasswordIncorrectException e) {
            throw new PasswordIncorrectException(e);
        }
    }
}

class SignUpPresenter implements SignUpUseCase.Presenter {
    private Student student;

    @Override
    public void setStudent(Student student) {
        this.student = student;
    }

    Student present() {
        return student;
    }
}

class SignInPresenter implements SignInUseCase.Presenter {
    private Student student;
    private final TokenService tokenService;

    public SignInPresenter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void setStudent(Student student) {
        this.student = student;
    }

    LoginResponse present() {
        TokenService.Token token = tokenService.createToken(new TokenService.Identity(student.getId()));
        return new LoginResponse(student.getId(), student.getEmail(), token.toString(), token.getExpiration().getTime());
    }
}

class GetStudentByIdPresenter implements GetStudentUseCase.Presenter {
    private Student student;

    @Override
    public void setStudent(Student student) {
        this.student = student;
    }

    Student present() {
        return student;
    }
}
