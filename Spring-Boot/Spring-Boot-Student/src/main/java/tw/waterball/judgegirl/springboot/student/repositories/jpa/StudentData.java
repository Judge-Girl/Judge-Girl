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

package tw.waterball.judgegirl.springboot.student.repositories.jpa;

import lombok.*;
import tw.waterball.judgegirl.entities.Admin;
import tw.waterball.judgegirl.entities.Student;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Builder
@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class StudentData {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    private String name;
    private String email;
    private String password;
    private boolean isAdmin;

    public Student toEntity() {
        if (isAdmin) {
            return new Admin(id, name, email, password);
        } else {
            return new Student(id, name, email, password);
        }
    }

    public static StudentData toData(Student student) {
        return StudentData.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .password(student.getPassword())
                .isAdmin(student.isAdmin())
                .build();
    }
}
