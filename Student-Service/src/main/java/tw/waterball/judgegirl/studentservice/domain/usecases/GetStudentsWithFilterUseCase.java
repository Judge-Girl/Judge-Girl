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
import lombok.Getter;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;
import java.util.List;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class GetStudentsWithFilterUseCase {
    private final StudentRepository studentRepository;

    public GetStudentsWithFilterUseCase(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public void execute(Request request, Presenter presenter) {
        List<Student> students = studentRepository.findStudents(request.skip, request.size, request.isAdmin);
        presenter.setStudents(students);
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Request {
        private int skip, size;
        private boolean isAdmin;
    }

    public interface Presenter {
        void setStudents(List<Student> students);
    }
}