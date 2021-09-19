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

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.springboot.helpers.SkipAndSizePageable;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.JpaStudentDAO;
import tw.waterball.judgegirl.springboot.student.repositories.jpa.StudentData;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.springboot.student.repositories.jpa.StudentData.toData;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Component
public class JpaStudentRepository implements StudentRepository {
    private final JpaStudentDAO jpaStudentDAO;

    public JpaStudentRepository(JpaStudentDAO jpaStudentDAO) {
        this.jpaStudentDAO = jpaStudentDAO;
    }

    @Override
    public Optional<Student> findByEmailAndPassword(String email, String pwd) {
        return jpaStudentDAO
                .findByEmailAndPassword(email, pwd)
                .map(StudentData::toEntity);
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        return jpaStudentDAO
                .findByEmail(email)
                .map(StudentData::toEntity);
    }

    @Override
    public List<Student> findByEmailIn(String... emails) {
        return mapToList(jpaStudentDAO.findByEmailIn(asList(emails)), StudentData::toEntity);
    }

    @Override
    public Optional<Student> findStudentById(Integer id) {
        return jpaStudentDAO
                .findStudentById(id)
                .map(StudentData::toEntity);
    }

    @Override
    public List<Student> findByIdIn(Integer... ids) {
        return mapToList(jpaStudentDAO.findByIdIn(asList(ids)), StudentData::toEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaStudentDAO.existsByEmail(email);
    }

    @Override
    public Student save(Student student) {
        StudentData data = jpaStudentDAO.save(toData(student));
        student.setId(data.getId());
        return student;
    }


    @Override
    public List<Student> findStudents(boolean admin, int skip, int size) {
        Pageable pageable = new SkipAndSizePageable(skip, size);
        List<StudentData> result;
        if (admin) {
            result = jpaStudentDAO.findByAdmin(admin, pageable).getContent();
        } else {
            result = jpaStudentDAO.findAll(pageable).getContent();
        }
        return result.stream()
                .map(StudentData::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStudentById(int studentId) {
        try {
            jpaStudentDAO.deleteById(studentId);
        } catch (EmptyResultDataAccessException ignored) {
        }
    }

    @Override
    public void deleteAll() {
        jpaStudentDAO.deleteAll();
    }
}
