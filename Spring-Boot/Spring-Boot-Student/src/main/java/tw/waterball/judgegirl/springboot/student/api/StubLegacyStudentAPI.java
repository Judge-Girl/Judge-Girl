/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.springboot.student.api;

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.profiles.Dev;
import tw.waterball.judgegirl.springboot.student.exceptions.PasswordIncorrectException;

import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Dev
@Component
public class StubLegacyStudentAPI implements LegacyStudentAPI {
    private final static Student STUB = new Student(1, "r12345678", "Stub");
    private final static String PASSWORD = "password";

    @Override
    public int authenticate(String account, String password) throws NotFoundException, PasswordIncorrectException {
        if (STUB.getAccount().equals(account)) {
            if (PASSWORD.equals(password)) {
                return STUB.getId();
            } else {
                throw new PasswordIncorrectException();
            }
        } else {
            throw NotFoundException.resource("Student")
                    .id(account);
        }
    }

    @Override
    public Optional<Student> getStudentByAccount(String account) {
        return STUB.getAccount().equals(account) ? Optional.of(STUB) : Optional.empty();
    }

    @Override
    public Optional<Student> getStudentById(int studentId) {
        return STUB.getId() == studentId ? Optional.of(STUB) : Optional.empty();
    }
}
