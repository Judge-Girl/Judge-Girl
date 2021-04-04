package tw.waterball.judgegirl.springboot.student.advices;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentPasswordIncorrectException;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.ChangePasswordUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.LoginUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.SignUpUseCase;

import static tw.waterball.judgegirl.springboot.student.view.StudentView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Aspect
@Component
@AllArgsConstructor
public class LoggingAspect {
    private final ObjectMapper objectMapper;

    @Around("bean(loginUseCase)")
    public Object logLoginUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var request = (LoginUseCase.Request) args[0];
        var presenter = (LoginUseCase.Presenter) args[1];
        var useCase = (LoginUseCase) joinPoint.getTarget();
        Logger log = LoggerFactory.getLogger(useCase.getClass());
        log.info("[Login] {\"email\":\"{}\"}", request.email);
        args[1] = new LoginUseCase.Presenter() {
            @Override
            public void loginSuccessfully(Student student) {
                log.info("[Login Successfully] {}", toJson(toViewModel(student)));
                presenter.loginSuccessfully(student);
            }

            @Override
            public void setToken(TokenService.Token token) {
            }
        };
        return joinPoint.proceed(args);
    }

    @Around("bean(signUpUseCase)")
    public Object logSignUpUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var request = (SignUpUseCase.Request) args[0];
        var presenter = (SignUpUseCase.Presenter) args[1];
        var useCase = (SignUpUseCase) joinPoint.getTarget();
        Logger log = LoggerFactory.getLogger(useCase.getClass());
        log.info("[SignUp] {\"name\"=\"{}\", \"email\"=\"{}\", \"admin\"={}}", request.name, request.email, request.admin);
        args[1] = (SignUpUseCase.Presenter) student -> {
            log.info("[SignUp Successfully] {}", toJson(toViewModel(student)));
            presenter.signUpSuccessfully(student);
        };
        return joinPoint.proceed(args);
    }

    @Around("bean(changePasswordUseCase)")
    public Object logChangePasswordUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var request = (ChangePasswordUseCase.Request) args[0];
        var useCase = (ChangePasswordUseCase) joinPoint.getTarget();
        Logger log = LoggerFactory.getLogger(useCase.getClass());
        log.info("[Change Password] {\"id\"={}}", request.studentId);
        try {
            Object response = joinPoint.proceed(args);
            log.info("[Change Password Successfully] {\"id\"={}}", request.studentId);
            return response;
        } catch (StudentPasswordIncorrectException err) {
            log.info("[Change Password Failed] {\"id\"={}, \"message\":\"{}\"}", request.studentId, err.getMessage());
            throw err;
        }
    }

    @SneakyThrows
    private String toJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }
}
