package tw.waterball.judgegirl.academy.domain.usecases.homework;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.Student;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.List;

/**
 * @author sh910913@gmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSubmissionRecord {
    public Student student;
    public List<SubmissionView> records;

    public int getStudentId() {
        return student.getId();
    }
}
