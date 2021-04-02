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

package tw.waterball.judgegirl.springboot.student.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.Student;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentView {
    public int id;
    public String name;
    public String email;
    public boolean admin;

    public static StudentView toViewModel(Student student) {
        return StudentView.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .admin(student.isAdmin())
                .build();
    }
}
