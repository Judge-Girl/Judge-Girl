package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author - sh91013@gmail.com (gordon.liao)
 */
@Getter
public class StudentsHomeworkProgress {

    // student's email --> student progress
    public Map<String, StudentProgress> progresses;

    public StudentsHomeworkProgress() {
        this.progresses = new HashMap<>();
    }

    public void addProgress(String studentEmail, StudentProgress studentProgress) {
        progresses.put(studentEmail, studentProgress);
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentProgress {

        public int studentId;
        public String studentName;
        // problem's id -> score
        public Map<Integer, Integer> problemScores;

        public StudentProgress(int studentId, String studentName) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.problemScores = new HashMap<>();
        }

        public void addProblemScore(int problemId, int problemScore) {
            problemScores.put(problemId, problemScore);
        }
    }

}
