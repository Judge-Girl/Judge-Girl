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

package tw.waterball.judgegirl.studentservice.domain.usecases.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import tw.waterball.judgegirl.entities.Admin;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateEmailException;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;
import tw.waterball.judgegirl.studentservice.ports.PasswordEncoder;

import javax.inject.Named;


/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
@AllArgsConstructor
public class SignUpUseCase {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public void execute(Request request, Presenter presenter) {
        validateEmail(request);
        validatePasswordLength(request.password);
        String encodedPassword = passwordEncoder.encode(request.password);
        Student student = createStudent(request, encodedPassword);
        student.validate();
        presenter.signUpSuccessfully(studentRepository.save(student));
    }

    private void validateEmail(Request request) {
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Duplicate email");
        }
    }

    private void validatePasswordLength(String password) {
        int length = password.length();
        if (length < 8 || length > 25) {
            throw new IllegalArgumentException();
        }
    }

    private Student createStudent(Request request, String encodedPassword) {
        return request.admin ? new Admin(request.name, request.email, encodedPassword) :
                new Student(request.name, request.email, encodedPassword);
    }

    public interface Presenter {
        void signUpSuccessfully(Student student);
    }

    @Data
    public static class Request {
        public String name;
        public String email;
        public String password;
        public boolean admin;
    }
}
