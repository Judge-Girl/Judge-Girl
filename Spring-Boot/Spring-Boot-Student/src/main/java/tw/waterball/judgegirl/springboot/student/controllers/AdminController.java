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

package tw.waterball.judgegirl.springboot.student.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.springboot.student.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.usecases.GetStudentsWithFilterUseCase;
import tw.waterball.judgegirl.studentservice.domain.usecases.SignUpUseCase;

import java.util.List;
import java.util.stream.Collectors;

import static tw.waterball.judgegirl.springboot.student.view.StudentView.toViewModel;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@CrossOrigin
@RequestMapping("/api/admins")
@RestController
@AllArgsConstructor
public class AdminController {
    private final GetStudentsWithFilterUseCase getStudentsWithFilterUseCase;
    private final SignUpUseCase signUpUseCase;

    @PostMapping
    public StudentView signUpAdmin(@RequestBody SignUpUseCase.Request request) {
        SignUpAdminPresenter presenter = new SignUpAdminPresenter();
        signUpUseCase.execute(request, presenter);
        return presenter.present();
    }

    @GetMapping
    public List<StudentView> getAdminsWithFilter(
            @RequestParam(defaultValue = "0", required = false) int skip,
            @RequestParam(defaultValue = "25", required = false) int size) {
        GetAdminsPresenter presenter = new GetAdminsPresenter();
        getStudentsWithFilterUseCase.execute(new GetStudentsWithFilterUseCase.Request(skip, size, true), presenter);
        return presenter.present();
    }
}

class GetAdminsPresenter implements GetStudentsWithFilterUseCase.Presenter {
    private List<Student> students;

    @Override
    public void setStudents(List<Student> students) {
        this.students = students;
    }

    List<StudentView> present() {
        return students.stream()
                .map(StudentView::toViewModel)
                .collect(Collectors.toList());
    }
}

class SignUpAdminPresenter implements SignUpUseCase.Presenter {
    private Student student;

    @Override
    public void setStudent(Student student) {
        this.student = student;
    }

    StudentView present() {
        return toViewModel(student);
    }
}
