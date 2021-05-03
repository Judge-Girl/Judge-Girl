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
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.studentapi.clients.view.StudentView;
import tw.waterball.judgegirl.studentservice.domain.usecases.student.SignUpUseCase;

import static tw.waterball.judgegirl.studentapi.clients.view.StudentView.toViewModel;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Component
public class SignUpPresenter implements SignUpUseCase.Presenter {
    private Student student;

    @Override
    public void signUpSuccessfully(Student student) {
        this.student = student;
    }

    public StudentView present() {
        return toViewModel(student);
    }
}