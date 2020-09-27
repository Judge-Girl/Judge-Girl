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

import tw.waterball.judgegirl.commons.entities.Student;
import tw.waterball.judgegirl.springboot.student.exceptions.AccountNotFoundException;
import tw.waterball.judgegirl.springboot.student.exceptions.PasswordIncorrectException;

import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface LegacyStudentAPI {
    /**
     * @return the authenticated student's id
     */
    int authenticate(String account, String password) throws AccountNotFoundException, PasswordIncorrectException;

    Optional<Student> getStudentByAccount(String account);

    Optional<Student> getStudentById(int studentId);
}
