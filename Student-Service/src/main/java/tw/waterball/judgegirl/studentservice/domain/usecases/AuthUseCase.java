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

package tw.waterball.judgegirl.studentservice.domain.usecases;

import lombok.Value;
import tw.waterball.judgegirl.commons.token.TokenInvalidException;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class AuthUseCase {
    private final StudentRepository studentRepository;

    public AuthUseCase(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void execute(Request request, Presenter presenter) throws TokenInvalidException {
        presenter.setStudent(studentRepository
                .findStudentById(request.id)
                .orElseThrow(TokenInvalidException::new));
    }

    @Value
    public static class Request {
        public Integer id;
    }

    public interface Presenter {
        void setStudent(Student student);

        void setToken(TokenService.Token token);
    }
}
