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

import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateEmailException;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class SignUpUseCase {
    private final StudentRepository studentRepository;

    public SignUpUseCase(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void execute(Request request, Presenter presenter) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Duplicate email");
        }
        Student student = new Student(request.name, request.email, request.password);
        presenter.setStudent(studentRepository.save(student));
    }

    public interface Presenter {
        void setStudent(Student student);
    }

    @Data
    @NoArgsConstructor
    public static class Request {
        @NotBlank
        public String name;
        @Email
        public String email;
        @NotBlank
        public String password;
    }
}
