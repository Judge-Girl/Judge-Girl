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

package tw.waterball.judgegirl.springboot.student.repositories;

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.JpaStudentDataPort;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.StudentData;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import java.util.Optional;

import static tw.waterball.judgegirl.springboot.student.repositories.jpa.StudentData.toData;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Component
public class JpaStudentRepository implements StudentRepository {
    private final JpaStudentDataPort jpaStudentDataPort;

    public JpaStudentRepository(JpaStudentDataPort jpaStudentDataPort) {
        this.jpaStudentDataPort = jpaStudentDataPort;
    }

    @Override
    public Optional<Student> findByEmailAndPassword(String email, String pwd) {
        return jpaStudentDataPort
                .findByEmailAndPassword(email, pwd)
                .map(StudentData::toEntity);
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        return jpaStudentDataPort
                .findByEmail(email)
                .map(StudentData::toEntity);
    }

    @Override
    public Optional<Student> findStudentById(Integer id) {
        return jpaStudentDataPort
                .findStudentById(id)
                .map(StudentData::toEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaStudentDataPort.existsByEmail(email);
    }

    @Override
    public Student save(Student student) {
        StudentData data = jpaStudentDataPort.saveAndFlush(toData(student));
        student.setId(data.getId());
        return student;
    }

    @Override
    public void deleteAll() {
        jpaStudentDataPort.deleteAll();
    }
}
