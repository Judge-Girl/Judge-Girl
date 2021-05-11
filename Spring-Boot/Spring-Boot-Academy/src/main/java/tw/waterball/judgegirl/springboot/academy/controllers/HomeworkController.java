package tw.waterball.judgegirl.springboot.academy.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.academy.domain.usecases.homework.*;
import tw.waterball.judgegirl.primitives.Homework;
import tw.waterball.judgegirl.springboot.academy.view.HomeworkProgress;
import tw.waterball.judgegirl.springboot.academy.view.HomeworkView;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.LinkedList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author - wally55077@gmail.com
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class HomeworkController {

    private final CreateHomeworkUseCase createHomeworkUseCase;
    private final GetHomeworkUseCase getHomeworkUseCase;
    private final GetHomeworkProgressUseCase getHomeworkProgressUseCase;
    private final GetAllHomeworkUseCase getAllHomeworkUseCase;
    private final DeleteHomeworkUseCase deleteHomeworkUseCase;

    @PostMapping("/homework")
    public HomeworkView createHomework(@RequestBody CreateHomeworkUseCase.Request request) {
        CreateHomeworkPresenter presenter = new CreateHomeworkPresenter();
        createHomeworkUseCase.execute(request, presenter);
        return presenter.present();
    }

    @GetMapping("/homework/{homeworkId}")
    public HomeworkView getHomework(@PathVariable int homeworkId) {
        GetHomeworkPresenter presenter = new GetHomeworkPresenter();
        getHomeworkUseCase.execute(homeworkId, presenter);
        return presenter.present();
    }

    @GetMapping("/homework")
    public List<HomeworkView> getAllHomework() {
        GetAllHomeworkPresenter presenter = new GetAllHomeworkPresenter();
        getAllHomeworkUseCase.execute(presenter);
        return presenter.present();
    }

    @DeleteMapping("/homework/{homeworkId}")
    public void deleteHomework(@PathVariable int homeworkId) {
        deleteHomeworkUseCase.execute(homeworkId);
    }

    @GetMapping("/students/{studentId}/homework/{homeworkId}/progress")
    public HomeworkProgress getHomeworkProgress(@PathVariable int studentId,
                                                @PathVariable int homeworkId) {
        GetHomeworkProgressPresenter presenter = new GetHomeworkProgressPresenter();
        getHomeworkProgressUseCase.execute(new GetHomeworkProgressUseCase.Request(studentId, homeworkId), presenter);
        return presenter.present();
    }

}

class CreateHomeworkPresenter implements CreateHomeworkUseCase.Presenter {

    private Homework homework;

    @Override
    public void showHomework(Homework homework) {
        this.homework = homework;
    }

    public HomeworkView present() {
        return HomeworkView.toViewModel(homework);
    }
}

class GetAllHomeworkPresenter implements GetAllHomeworkUseCase.Presenter {

    private List<Homework> allHomework;

    @Override
    public void showAllHomework(List<Homework> allHomework) {
        this.allHomework = allHomework;
    }

    public List<HomeworkView> present() {
        return allHomework.stream().map(HomeworkView::toViewModel).collect(toList());
    }
}

class GetHomeworkPresenter implements GetHomeworkUseCase.Presenter {

    private Homework homework;

    @Override
    public void showHomework(Homework homework) {
        this.homework = homework;
    }

    public HomeworkView present() {
        return HomeworkView.toViewModel(homework);
    }
}

class GetHomeworkProgressPresenter implements GetHomeworkProgressUseCase.Presenter {

    private final List<SubmissionView> progress = new LinkedList<>();
    private Homework homework;

    @Override
    public void showHomework(Homework homework) {
        this.homework = homework;
    }

    @Override
    public void showProgress(SubmissionView progress) {
        this.progress.add(progress);
    }

    public HomeworkProgress present() {
        return HomeworkProgress.toViewModel(homework, progress);
    }

}

