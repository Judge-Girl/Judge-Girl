package tw.waterball.judgegirl.academy.domain.usecases.homework;

import tw.waterball.judgegirl.primitives.Student;

import java.util.List;

/**
 * @author sh910913@gmail.com
 */
public interface HomeworkProgressPresenter {

    void showProblems(List<Integer> problemIds);

    void showStudents(List<Student> students);

    void showRecords(List<StudentSubmissionRecord> records);
}
