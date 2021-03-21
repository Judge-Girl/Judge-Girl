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
import tw.waterball.judgegirl.entities.Admin;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.exceptions.DuplicateEmailException;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;


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
        Student student = createStudent(request);
        student.validate();
        presenter.setStudent(studentRepository.save(student));
    }

    private Student createStudent(Request request) {
        return request.isAdmin ? new Admin(request.name, request.email, request.password) :
                new Student(request.name, request.email, request.password);
    }

    public interface Presenter {
        void setStudent(Student student);
    }

    @Data
    public static class Request {
        public String name;
        public String email;
        public String password;
        public boolean isAdmin;
    }
}
