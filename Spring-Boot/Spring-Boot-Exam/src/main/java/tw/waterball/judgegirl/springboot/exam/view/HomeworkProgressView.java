package tw.waterball.judgegirl.springboot.exam.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.entities.HomeworkProgress;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author - wally55077@gmail.com
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeworkProgressView {

    public HomeworkView homework;

    public Map<Integer, BestRecord> progress = new TreeMap<>();

    public static HomeworkProgressView toViewModel(HomeworkProgress homeworkProgress) {
        HomeworkView homework = HomeworkView.toViewModel(homeworkProgress.getHomework());
        Map<Integer, BestRecord> progress = new TreeMap<>();
        homeworkProgress.getProgress()
                .forEach((problemId, verdict) -> progress.put(problemId, new BestRecord(VerdictView.toViewModel(verdict))));
        return new HomeworkProgressView(homework, progress);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BestRecord {
        private VerdictView bestRecord;
    }

}
