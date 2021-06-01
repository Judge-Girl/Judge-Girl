package tw.waterball.judgegirl.springboot.academy.view;

import lombok.*;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

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

    // student's email --> examineeRecord
    public Map<String, ExamineeRecordView> scoreBoard;

    public List<RecordView> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExamineeRecordView {
        // problem's id -> score
        @Singular
        public Map<Integer, Integer> questionScores;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecordView {
        public int examId;
        public int problemId;
        public SubmissionView submission;
    }

}
