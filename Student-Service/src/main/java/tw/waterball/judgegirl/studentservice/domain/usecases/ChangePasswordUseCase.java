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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.exceptions.StudentPasswordIncorrectException;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
@AllArgsConstructor
public class ChangePasswordUseCase {
    private final GetStudentUseCase getStudentUseCase;
    private final StudentRepository studentRepository;

    public void execute(Request request) {
        Student student = getStudentUseCase.execute(request.studentId);
        validatePassword(request.currentPwd, student.getPassword());
        student.setPassword(request.newPwd);
        studentRepository.save(student);
    }

    private void validatePassword(String studentPwd, String requestPwd) throws StudentPasswordIncorrectException {
        if (!studentPwd.equals(requestPwd)) {
            throw new StudentPasswordIncorrectException();
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request {
        public int studentId;
        public String currentPwd;
        public String newPwd;
    }

}