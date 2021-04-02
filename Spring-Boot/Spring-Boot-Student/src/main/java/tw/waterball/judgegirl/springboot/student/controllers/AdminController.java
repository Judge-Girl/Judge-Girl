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
import tw.waterball.judgegirl.springboot.student.presenters.GetStudentsPresenter;
import tw.waterball.judgegirl.springboot.student.presenters.SignInPresenter;
import tw.waterball.judgegirl.springboot.student.presenters.SignUpPresenter;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.usecases.GetStudentsWithFilterUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignInUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignUpUseCase;

import java.util.List;

import static tw.waterball.judgegirl.commons.token.TokenService.Identity.admin;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@CrossOrigin
@RequestMapping("/api/admins")
@RestController
@AllArgsConstructor
public class AdminController {
    private final GetStudentsWithFilterUseCase getStudentsWithFilterUseCase;
    private final SignUpUseCase signUpUseCase;
    private final SignInUseCase signInUseCase;
    private final TokenService tokenService;

    @PostMapping
    public StudentView signUp(@RequestBody SignUpUseCase.Request request) {
        SignUpPresenter presenter = new SignUpPresenter();
        request.admin = true;
        signUpUseCase.execute(request, presenter);
        return presenter.present();
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody SignInUseCase.Request request) {
        SignInPresenter presenter = new SignInPresenter();
        signInUseCase.execute(request, presenter);
        presenter.setToken(tokenService.createToken(admin()));
        return presenter.present();
    }

    @GetMapping
    public List<StudentView> getAdminsWithFilter(
            @RequestParam(defaultValue = "0", required = false) int skip,
            @RequestParam(defaultValue = "25", required = false) int size) {
        GetStudentsPresenter presenter = new GetStudentsPresenter();
        getStudentsWithFilterUseCase.execute(new GetStudentsWithFilterUseCase.Request(true, skip, size), presenter);
        return presenter.present();
    }
}
