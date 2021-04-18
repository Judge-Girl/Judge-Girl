package tw.waterball.judgegirl.examservice.domain.usecases.homework;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Homework;
import tw.waterball.judgegirl.entities.HomeworkProgress;
import tw.waterball.judgegirl.entities.submission.verdict.Verdict;
import tw.waterball.judgegirl.examservice.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;
import tw.waterball.judgegirl.submissionapi.views.VerdictView;

import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetHomeworkProgressUseCase {

    private final HomeworkRepository homeworkRepository;

    private final SubmissionServiceDriver submissionServiceDriver;

    public void execute(Request request, Presenter presenter)
            throws NotFoundException {
        Homework homework = homeworkRepository.findHomeworkById(request.homeworkId)
                .orElseThrow(NotFoundException::new);
        Map<Integer, Verdict> allBestRecord = findAllBestRecord(request.studentId, homework.getProblemIds());
        HomeworkProgress homeworkProgress = new HomeworkProgress(homework, allBestRecord);
        presenter.showHomeworkProgress(homeworkProgress);
    }

    private Map<Integer, Verdict> findAllBestRecord(int studentId, List<Integer> problemIds) {
        Map<Integer, Verdict> allBestRecord = new TreeMap<>();
        problemIds.forEach(problemId -> findBestRecord(studentId, problemId)
                .ifPresent(submission -> allBestRecord.put(problemId, VerdictView.toEntity(submission.verdict))));
        return allBestRecord;
    }

    private Optional<SubmissionView> findBestRecord(int studentId, int problemId) {
        try {
            return Optional.of(submissionServiceDriver.findBestRecord(problemId, studentId));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    public interface Presenter {

        void showHomeworkProgress(HomeworkProgress homeworkProgress);

    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int studentId;
        public int homeworkId;
    }

}
