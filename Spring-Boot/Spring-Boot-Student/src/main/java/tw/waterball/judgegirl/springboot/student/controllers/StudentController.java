package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.commons.token.TokenInvalidException;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.commons.utils.HttpHeaderUtils;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentEmailNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentIdNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentPasswordIncorrectException;
import tw.waterball.judgegirl.studentservice.domain.usecases.AuthUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.GetStudentUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignInUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignUpUseCase;

import static tw.waterball.judgegirl.springboot.student.view.StudentView.toViewModel;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@RequestMapping("/api/students")
@RestController
@AllArgsConstructor
public class StudentController {
    private final SignInUseCase signInUseCase;
    private final SignUpUseCase signUpUseCase;
    private final GetStudentUseCase getStudentUseCase;
    private final AuthUseCase authUseCase;
    private final TokenService tokenService;

    @PostMapping("/signUp")
    public StudentView signUp(@RequestBody SignUpUseCase.Request request) {
        SignUpPresenter presenter = new SignUpPresenter();
        signUpUseCase.execute(request, presenter);
        return presenter.present();
    }

    //TODO: add auth
    @PostMapping("/login")
    public LoginResponse login(@RequestBody SignInUseCase.Request request) {
        SignInPresenter presenter = new SignInPresenter(tokenService);
        signInUseCase.execute(request, presenter);
        return presenter.present();
    }

    @GetMapping("{studentId}")
    public StudentView getStudent(@PathVariable Integer studentId) {
        GetStudentByIdPresenter presenter = new GetStudentByIdPresenter();
        getStudentUseCase.execute(new GetStudentUseCase.Request(studentId), presenter);
        return presenter.present();
    }

    @PostMapping("/auth")
    public LoginResponse auth(@RequestHeader("Authorization") String authorization) {
        String tokenString = HttpHeaderUtils.parseBearerToken(authorization);
        AuthPresenter presenter = new AuthPresenter();
        authUseCase.execute(new AuthUseCase.Request(tokenString), presenter);

        return presenter.present();
    }

    @ExceptionHandler({StudentPasswordIncorrectException.class})
    public ResponseEntity<?> badRequestHandler(Exception err) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler({StudentIdNotFoundException.class, StudentEmailNotFoundException.class})
    public ResponseEntity<?> notFoundHandler(Exception err) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({TokenInvalidException.class})
    public ResponseEntity<?> unauthorizedHandler(Exception err) {
        return ResponseEntity.status(401).build();
    }
}

class SignUpPresenter implements SignUpUseCase.Presenter {
    private Student student;

    @Override
    public void setStudent(Student student) {
        this.student = student;
    }

    StudentView present() {
        return toViewModel(student);
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

    StudentView present() {
        return toViewModel(student);
    }
}

class AuthPresenter implements AuthUseCase.Presenter {
    private String email;
    private TokenService.Token token;

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public void setToken(TokenService.Token token) {
        this.token = token;
    }

    LoginResponse present() {
        return new LoginResponse(token.getStudentId(), email, token.toString(), token.getExpiration().getTime());
    }
}
