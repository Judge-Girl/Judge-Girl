/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.commons.token.TokenInvalidException;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateEmailException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentEmailNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentIdNotFoundException;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentPasswordIncorrectException;
import tw.waterball.judgegirl.studentservice.domain.usecases.*;

import static tw.waterball.judgegirl.springboot.student.view.StudentView.toViewModel;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@CrossOrigin
@RequestMapping("/api/students")
@RestController
@AllArgsConstructor
public class StudentController {
    private final SignInUseCase signInUseCase;
    private final SignUpUseCase signUpUseCase;
    private final GetStudentUseCase getStudentUseCase;
    private final AuthUseCase authUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final TokenService tokenService;

    @PostMapping("/signUp")
    public StudentView signUp(@RequestBody SignUpUseCase.Request request) {
        SignUpPresenter presenter = new SignUpPresenter();
        signUpUseCase.execute(request, presenter);
        return presenter.present();
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody SignInUseCase.Request request) {
        SignInPresenter presenter = new SignInPresenter();
        signInUseCase.execute(request, presenter);
        presenter.setToken(tokenService.createToken(new TokenService.Identity(presenter.getStudentId())));
        return presenter.present();
    }

    @GetMapping("{studentId}")
    public StudentView getStudentById(@PathVariable Integer studentId, @RequestHeader("Authorization") String authorization) {
        return tokenService.returnIfTokenValid(studentId, authorization,
                token -> {
                    GetStudentByIdPresenter presenter = new GetStudentByIdPresenter();
                    getStudentUseCase.execute(studentId, presenter);
                    return presenter.present();
                });
    }

    @PostMapping("/auth")
    public LoginResponse auth(@RequestHeader("Authorization") String authorization) {
        TokenService.Token token = tokenService.parseBearerTokenAndValidate(authorization);

        AuthPresenter presenter = new AuthPresenter();
        presenter.setToken(tokenService.renewToken(token.getToken()));
        authUseCase.execute(new AuthUseCase.Request(token.getStudentId()), presenter);

        return presenter.present();
    }

    @PatchMapping("/{studentId}/password")
    public void changePassword(
            @PathVariable Integer studentId,
            @RequestBody ChangePasswordUseCase.Request request,
            @RequestHeader("Authorization") String authorization) {
        request.studentId = studentId;
        tokenService.ifTokenValid(studentId, authorization,
                token -> changePasswordUseCase.execute(request));
    }


    @ExceptionHandler({StudentPasswordIncorrectException.class, DuplicateEmailException.class, IllegalArgumentException.class})
    public ResponseEntity<?> badRequestHandler(Exception err) {
        return ResponseEntity.badRequest().body(err.getMessage());
    }

    @ExceptionHandler({StudentIdNotFoundException.class, StudentEmailNotFoundException.class})
    public ResponseEntity<?> notFoundHandler(Exception err) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler({TokenInvalidException.class})
    public ResponseEntity<?> unauthorizedHandler(Exception err) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
    private TokenService.Token token;

    @Override
    public void setToken(TokenService.Token token) {
        this.token = token;
    }

    @Override
    public void setStudent(Student student) {
        this.student = student;
    }

    Integer getStudentId() {
        return student.getId();
    }

    LoginResponse present() {
        return new LoginResponse(student.getId(), student.getEmail(), token.toString(),
                token.getExpiration().getTime(), student.isAdmin());
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
    private Student student;
    private TokenService.Token token;

    @Override
    public void setStudent(Student student) {
        this.student = student;
    }

    @Override
    public void setToken(TokenService.Token token) {
        this.token = token;
    }

    LoginResponse present() {
        return new LoginResponse(token.getStudentId(), student.getEmail(), token.toString(),
                token.getExpiration().getTime(), student.isAdmin());
    }
}
