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

package tw.waterball.judgegirl.studentservice.domain.repositories;

import tw.waterball.judgegirl.entities.Student;

import java.util.List;
import java.util.Optional;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
public interface StudentRepository {
    Optional<Student> findByEmailAndPassword(String email, String pwd);

    Optional<Student> findByEmail(String email);

    List<Student> findByEmailIn(String... emails);

    Optional<Student> findStudentById(Integer id);

    List<Student> findByIdIn(Integer... ids);

    boolean existsByEmail(String email);

    Student save(Student student);

    void deleteAll();

    List<Student> findStudents(boolean admin, int skip, int size);

}
