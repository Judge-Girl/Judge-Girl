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

import tw.waterball.judgegirl.entities.Student;
import tw.waterball.judgegirl.studentservice.domain.repositories.StudentRepository;

import javax.inject.Named;
import java.util.List;

import static java.util.Arrays.asList;
import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author chaoyulee chaoyu2330@gmail.com
 */
@Named
public class GetStudentUseCase {
    private final StudentRepository studentRepository;

    public GetStudentUseCase(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student execute(int studentId) {
        DefaultGetStudentPresenter presenter = new DefaultGetStudentPresenter();
        execute(studentId, presenter);
        return presenter.getStudents().get(0);
    }

    public void execute(int studentId, Presenter presenter) {
        var student = studentRepository.findStudentById(studentId)
                .orElseThrow(() -> notFound(Student.class).id(studentId));
        presenter.showStudents(student);
    }

    public void execute(Request request, Presenter presenter) {
        presenter.showStudents(studentRepository
                .findByIdIn(request.ids));
    }

    public static class Request {
        public Integer[] ids;

        public Request(Integer... ids) {
            this.ids = ids;
        }
    }

    public interface Presenter {
        void showStudents(List<Student> students);

        default void showStudents(Student... students) {
            showStudents(asList(students));
        }
    }
}