package tw.waterball.judgegirl.springboot.academy.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.primitives.Homework;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author - wally55077@gmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkProgress {

    public HomeworkView homework;

    public Map<Integer, BestRecord> progress = new TreeMap<>();

    public static HomeworkProgress toViewModel(Homework homework, List<SubmissionView> progress) {
        HomeworkProgress homeworkProgress = new HomeworkProgress();
        homeworkProgress.homework = HomeworkView.toViewModel(homework);
        progress.forEach(submission -> homeworkProgress.progress.put(submission.problemId, new BestRecord(submission.verdict)));
        return homeworkProgress;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BestRecord {
        private VerdictView bestRecord;
    }

}
