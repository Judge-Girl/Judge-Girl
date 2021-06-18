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

package tw.waterball.judgegirl.springboot.student.presenters;

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.springboot.student.controllers.LoginResponse;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.LoginUseCase;

import java.util.Date;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Component
public class SignInPresenter implements LoginUseCase.Presenter {
    private Student student;
    private TokenService.Token token;

    @Override
    public void setToken(TokenService.Token token) {
        this.token = token;
    }

    @Override
    public void loginSuccessfully(Student student) {
        this.student = student;
    }

    public boolean isAdmin() {
        return student.isAdmin();
    }

    public int getStudentId() {
        return student.getId();
    }

    public LoginResponse present() {
        Date expiration = token.getExpiration();
        if (expiration == null) {
            throw new RuntimeException("token expiration is null");
        }
        return new LoginResponse(student.getId(), student.getEmail(), token.toString(),
                expiration.getTime(), student.isAdmin());
    }
}