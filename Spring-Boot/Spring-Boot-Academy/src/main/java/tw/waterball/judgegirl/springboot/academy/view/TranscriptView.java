package tw.waterball.judgegirl.springboot.academy.view;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranscriptView {
    private double averageScore;
    private int maxScore;

    // student's email --> examineeRecord
    @Singular
    public Map<String, ExamineeRecord> examineeRecords;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamineeRecord {
        public int totalScore;
        public List<Integer> questionScores;
    }
}
