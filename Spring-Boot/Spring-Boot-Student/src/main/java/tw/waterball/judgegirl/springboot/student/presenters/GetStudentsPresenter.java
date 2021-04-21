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

package tw.waterball.judgegirl.springboot.student.presenters;

import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.GetStudentsWithFilterUseCase;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Component
public class GetStudentsPresenter implements GetStudentsWithFilterUseCase.Presenter {
    private List<Student> students;

    @Override
    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<StudentView> present() {
        return students.stream()
                .map(StudentView::toViewModel)
                .collect(Collectors.toList());
    }
}