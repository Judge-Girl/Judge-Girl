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
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.presenters.GetStudentsPresenter;
import tw.waterball.judgegirl.springboot.student.presenters.SignInPresenter;
import tw.waterball.judgegirl.springboot.student.presenters.SignUpPresenter;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.*;

import java.util.List;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.commons.token.TokenService.Identity.admin;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.student;
import static tw.waterball.judgegirl.springboot.student.view.StudentView.toViewModel;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@CrossOrigin
@RequestMapping("/api/students")
@RestController
@AllArgsConstructor
public class StudentController {
    private final LoginUseCase loginUseCase;
    private final SignUpUseCase signUpUseCase;
    private final GetStudentUseCase getStudentUseCase;
    private final GetStudentsByEmailListUseCase getStudentsByEmailListUseCase;
    private final GetStudentsWithFilterUseCase getStudentsWithFilterUseCase;
    private final AuthUseCase authUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final TokenService tokenService;

    @PostMapping
    public StudentView signUp(@RequestBody SignUpUseCase.Request request) {
        SignUpPresenter presenter = new SignUpPresenter();
        request.admin = false;
        signUpUseCase.execute(request, presenter);
        return presenter.present();
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginUseCase.Request request) {
        SignInPresenter presenter = new SignInPresenter();
        loginUseCase.execute(request, presenter);
        presenter.setToken(tokenService.createToken(
                presenter.isAdmin() ? admin(presenter.getStudentId()) : student(presenter.getStudentId())));
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

    @PostMapping("/search")
    public List<StudentView> getStudentsByEmailList(@RequestBody String[] emails) {
        GetStudentsByEmailListPresenter presenter = new GetStudentsByEmailListPresenter();
        getStudentsByEmailListUseCase.execute(new GetStudentsByEmailListUseCase.Request(emails), presenter);
        return presenter.present();
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

    @GetMapping
    public List<StudentView> getStudentsWithFilter(
            @RequestParam(defaultValue = "0", required = false) int skip,
            @RequestParam(defaultValue = "25", required = false) int size) {
        GetStudentsPresenter presenter = new GetStudentsPresenter();
        getStudentsWithFilterUseCase.execute(new GetStudentsWithFilterUseCase.Request(false, skip, size), presenter);
        return presenter.present();
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

class GetStudentsByEmailListPresenter implements GetStudentsByEmailListUseCase.Presenter {
    private List<Student> students;

    @Override
    public void setStudents(List<Student> students) {
        this.students = students;
    }

    List<StudentView> present() {
        return students.stream()
                .map(StudentView::toViewModel)
                .collect(Collectors.toList());
    }
}

