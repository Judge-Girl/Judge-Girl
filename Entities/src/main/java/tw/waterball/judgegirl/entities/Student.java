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

package tw.waterball.judgegirl.entities;

import lombok.*;
import tw.waterball.judgegirl.commons.utils.JSR380Utils;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    protected Integer id;
    @NotBlank
    protected String name;
    @Email
    protected String email;
    @NotBlank
    protected String password;

    protected boolean isAdmin = false;

    private final Set<Group> groups = new HashSet<>();

    public Student(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Student(Integer id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void validate() {
        JSR380Utils.validate(this);
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void deleteGroup(Group group) {
        groups.remove(group);
    }
}
