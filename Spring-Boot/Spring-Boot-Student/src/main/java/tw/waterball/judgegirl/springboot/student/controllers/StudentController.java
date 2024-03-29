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
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.springboot.student.presenters.GetStudentsPresenter;
import tw.waterball.judgegirl.springboot.student.presenters.SignInPresenter;
import tw.waterball.judgegirl.springboot.student.presenters.SignUpPresenter;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.commons.token.TokenService.Identity.admin;
import static tw.waterball.judgegirl.commons.token.TokenService.Identity.student;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

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
    private final DeleteStudentUseCase deleteStudentUseCase;
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
        return tokenService.returnIfGranted(studentId, authorization,
                token -> {
                    GetStudentByIdsPresenter presenter = new GetStudentByIdsPresenter();
                    getStudentUseCase.execute(studentId, presenter);
                    return presenter.present().get(0);
                });
    }

    @PostMapping("/search")
    public List<StudentView> getStudentsByEmailList(@RequestBody String[] emails) {
        GetStudentsByEmailListPresenter presenter = new GetStudentsByEmailListPresenter();
        getStudentsByEmailListUseCase.execute(emails, presenter);
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

    @DeleteMapping("/{studentId}")
    public void delete(@PathVariable Integer studentId,
                       @RequestHeader("Authorization") String authorization) {
        tokenService.ifAdminToken(authorization, token -> deleteStudentUseCase.execute(studentId));
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
            @RequestParam(defaultValue = "25", required = false) int size,
            @RequestParam(required = false) Integer[] ids) {
        if (ids == null) {
            GetStudentsPresenter presenter = new GetStudentsPresenter();
            getStudentsWithFilterUseCase.execute(new GetStudentsWithFilterUseCase.Request(false, skip, size), presenter);
            return presenter.present();
        }
        GetStudentByIdsPresenter presenter = new GetStudentByIdsPresenter();
        getStudentUseCase.execute(new GetStudentUseCase.Request(ids), presenter);
        return presenter.present();
    }
}

class GetStudentByIdsPresenter implements GetStudentUseCase.Presenter {
    private final List<Student> students = new ArrayList<>();

    List<StudentView> present() {
        return mapToList(students, StudentView::toViewModel);
    }

    @Override
    public void showStudents(List<Student> students) {
        this.students.addAll(students);
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
        Date expiration = token.getExpiration();
        if (expiration == null) {
            throw new RuntimeException("token expiration is null");
        }
        return new LoginResponse(token.getStudentId(), student.getEmail(), token.toString(),
                expiration.getTime(), student.isAdmin());
    }
}

class GetStudentsByEmailListPresenter implements GetStudentsByEmailListUseCase.Presenter {
    private List<Student> students;

    @Override
    public void showStudents(List<Student> students) {
        this.students = students;
    }

    List<StudentView> present() {
        return students.stream()
                .map(StudentView::toViewModel)
                .collect(Collectors.toList());
    }
}

