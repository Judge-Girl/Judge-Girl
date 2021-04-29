package tw.waterball.judgegirl.examservice.domain.usecases.homework;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Homework;
import tw.waterball.judgegirl.examservice.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.submissionapi.clients.SubmissionServiceDriver;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import javax.inject.Named;
import java.util.Optional;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

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
        Homework homework = findHomework(request);
        presenter.showHomework(homework);
        showBestRecords(request, homework, presenter);
    }

    private Homework findHomework(Request request) {
        return homeworkRepository.findHomeworkById(request.homeworkId)
                .orElseThrow(() -> notFound(Homework.class).id(request.homeworkId));
    }

    private void showBestRecords(Request request, Homework homework, Presenter presenter) {
        homework.getProblemIds().stream()
                .flatMap(problemId -> findBestRecord(request.studentId, problemId).stream())
                .forEach(presenter::showProgress);
    }

    private Optional<SubmissionView> findBestRecord(int studentId, int problemId) {
        try {
            return Optional.of(submissionServiceDriver.findBestRecord(problemId, studentId));
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    public interface Presenter {

        void showHomework(Homework homework);

        void showProgress(SubmissionView progress);

    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int studentId;
        public int homeworkId;
    }

}
