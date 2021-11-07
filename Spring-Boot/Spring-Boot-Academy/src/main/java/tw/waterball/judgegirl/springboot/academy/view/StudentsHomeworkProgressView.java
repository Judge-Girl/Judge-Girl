package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author - sh91013@gmail.com (gordon.liao)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentsHomeworkProgressView {
    // student's email --> student progress
    public Map<String, StudentProgress> progress;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentProgress {

        public int studentId;
        public String studentName;
        // problem's id -> score
        public Map<Integer, Integer> problemScores;
    }

}
