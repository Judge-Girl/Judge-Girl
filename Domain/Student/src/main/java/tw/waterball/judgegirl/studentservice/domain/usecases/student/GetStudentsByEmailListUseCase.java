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

package tw.waterball.judgegirl.studentservice.domain.usecases.student;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;
import java.util.List;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
@AllArgsConstructor
public class GetStudentsByEmailListUseCase {
    private final StudentRepository studentRepository;

    public void execute(String[] emails, Presenter presenter) {
        List<Student> students = studentRepository.findByEmailIn(emails);
        presenter.showStudents(students);
    }

    public interface Presenter {
        void showStudents(List<Student> students);
    }
}
