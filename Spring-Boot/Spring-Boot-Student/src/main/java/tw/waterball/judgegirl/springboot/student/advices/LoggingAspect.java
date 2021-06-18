package tw.waterball.judgegirl.springboot.student.advices;

import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.studentservice.domain.exceptions.ForbiddenLoginException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentPasswordIncorrectException;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.ChangePasswordUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.LoginUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.SignUpUseCase;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Aspect
@Component
@AllArgsConstructor
public class LoggingAspect {
    @Around("bean(loginUseCase)")
    public Object logLoginUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var request = (LoginUseCase.Request) args[0];
        var presenter = (LoginUseCase.Presenter) args[1];
        var useCase = (LoginUseCase) joinPoint.getTarget();
        Logger log = LoggerFactory.getLogger(useCase.getClass());
        log.info("[Login] email={} admin={}", request.email, request.admin);
        args[1] = new LoginUseCase.Presenter() {
            @Override
            public void loginSuccessfully(Student student) {
                log.info("[Login Successfully] id={} email={} name={}", student.getId(), student.getEmail(), student.getName());
                presenter.loginSuccessfully(student);
            }

            @Override
            public void setToken(TokenService.Token token) {
            }
        };

        try {
            return joinPoint.proceed(args);
        } catch (ForbiddenLoginException err) {
            log.warn("[Login Failed]");
            throw err;
        }
    }

    @Around("bean(signUpUseCase)")
    public Object logSignUpUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var request = (SignUpUseCase.Request) args[0];
        var useCase = (SignUpUseCase) joinPoint.getTarget();
        Logger log = LoggerFactory.getLogger(useCase.getClass());
        log.info("[Sign Up] name=\"{}\" email=\"{}\" admin={}", request.name, request.email, request.admin);
        return joinPoint.proceed(args);
    }

    @Around("bean(changePasswordUseCase)")
    public Object logChangePasswordUseCase(ProceedingJoinPoint joinPoint) throws Throwable {
        var args = joinPoint.getArgs();
        var request = (ChangePasswordUseCase.Request) args[0];
        var useCase = (ChangePasswordUseCase) joinPoint.getTarget();
        Logger log = LoggerFactory.getLogger(useCase.getClass());
        log.info("[Change Password] id={}", request.studentId);
        try {
            return joinPoint.proceed(args);
        } catch (StudentPasswordIncorrectException err) {
            log.warn("[Change Password Failed] id={} message=\"{}\"}", request.studentId, err.getMessage());
            throw err;
        }
    }
}
