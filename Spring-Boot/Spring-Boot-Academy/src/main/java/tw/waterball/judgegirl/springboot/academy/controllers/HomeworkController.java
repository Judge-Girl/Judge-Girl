package tw.waterball.judgegirl.springboot.academy.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.academy.domain.usecases.homework.*;
import tw.waterball.judgegirl.commons.token.TokenService;
import tw.waterball.judgegirl.primitives.Homework;
import tw.waterball.judgegirl.springboot.academy.presenters.GroupsHomeworkProgressPresenter;
import tw.waterball.judgegirl.springboot.academy.presenters.StudentsHomeworkProgressPresenter;
import tw.waterball.judgegirl.springboot.academy.view.HomeworkProgress;
import tw.waterball.judgegirl.springboot.academy.view.HomeworkView;
import tw.waterball.judgegirl.springboot.academy.view.StudentsHomeworkProgress;
import tw.waterball.judgegirl.submissionapi.views.SubmissionView;

import java.util.LinkedList;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;

/**
 * @author - wally55077@gmail.com
 */
@CrossOrigin
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class HomeworkController {
    private final TokenService tokenService;
    private final CreateHomeworkUseCase createHomeworkUseCase;
    private final GetHomeworkUseCase getHomeworkUseCase;
    private final GetHomeworkProgressUseCase getHomeworkProgressUseCase;
    private final GetAllHomeworkUseCase getAllHomeworkUseCase;
    private final DeleteHomeworkUseCase deleteHomeworkUseCase;
    private final AddHomeworkProblemsUseCase addHomeworkProblemUseCase;
    private final DeleteHomeworkProblemsUseCase deleteHomeworkProblemsUseCase;
    private final GetStudentsHomeworkProgressUseCase getStudentsHomeworkProgressUseCase;
    private final GetGroupsHomeworkProgressUseCase getGroupsHomeworkProgressUseCase;

    @PostMapping("/homework")
    public HomeworkView createHomework(@RequestHeader("Authorization") String authorization,
                                       @RequestBody CreateHomeworkUseCase.Request request) {
        return tokenService.returnIfAdmin(authorization, token -> {
            CreateHomeworkPresenter presenter = new CreateHomeworkPresenter();
            createHomeworkUseCase.execute(request, presenter);
            return presenter.present();
        });
    }

    @GetMapping("/homework/{homeworkId}")
    public HomeworkView getHomework(@RequestHeader("Authorization") String authorization,
                                    @PathVariable int homeworkId) {
        return tokenService.returnIfAdmin(authorization, token -> {
            GetHomeworkPresenter presenter = new GetHomeworkPresenter();
            getHomeworkUseCase.execute(homeworkId, presenter);
            return presenter.present();
        });
    }

    @GetMapping("/homework")
    public List<HomeworkView> getAllHomework(@RequestHeader("Authorization") String authorization) {
        return tokenService.returnIfAdmin(authorization, token -> {
            GetAllHomeworkPresenter presenter = new GetAllHomeworkPresenter();
            getAllHomeworkUseCase.execute(presenter);
            return presenter.present();
        });
    }

    @DeleteMapping("/homework/{homeworkId}")
    public void deleteHomework(@RequestHeader("Authorization") String authorization,
                               @PathVariable int homeworkId) {
        tokenService.ifAdminToken(authorization,
                token -> deleteHomeworkUseCase.execute(homeworkId));
    }

    @GetMapping("/students/{studentId}/homework/{homeworkId}/progress")
    public HomeworkProgress getHomeworkProgress(@RequestHeader("Authorization") String authorization,
                                                @PathVariable int studentId,
                                                @PathVariable int homeworkId) {
        return tokenService.returnIfAdmin(authorization, token -> {
            GetHomeworkProgressPresenter presenter = new GetHomeworkProgressPresenter();
            getHomeworkProgressUseCase.execute(new GetHomeworkProgressUseCase.Request(studentId, homeworkId), presenter);
            return presenter.present();
        });
    }

    @PostMapping("/homework/{homeworkId}/problems")
    public void addProblemsIntoHomework(@RequestHeader("Authorization") String authorization,
                                        @PathVariable int homeworkId, @RequestBody Integer[] problemIds) {
        tokenService.ifAdminToken(authorization,
                token -> addHomeworkProblemUseCase.execute(new AddHomeworkProblemsUseCase.Request(homeworkId, problemIds)));
    }

    @DeleteMapping("/homework/{homeworkId}/problems")
    public void deleteProblemsFromTheHomework(@RequestHeader("Authorization") String authorization,
                                              @PathVariable int homeworkId, @RequestBody Integer[] problemIds) {
        tokenService.ifAdminToken(authorization,
                token -> deleteHomeworkProblemsUseCase.execute(new DeleteHomeworkProblemsUseCase.Request(homeworkId, problemIds)));
    }

    @PostMapping("/students/homework/{homeworkId}/progress")
    public StudentsHomeworkProgress getStudentsHomeworkProgress(@RequestHeader("Authorization") String authorization,
                                                                @PathVariable int homeworkId,
                                                                @RequestBody List<String> emails) {
        return tokenService.returnIfAdmin(authorization, token -> {
            var presenter = new StudentsHomeworkProgressPresenter();
            getStudentsHomeworkProgressUseCase.execute(new GetStudentsHomeworkProgressUseCase.Request(homeworkId, emails), presenter);
            return presenter.present();
        });
    }

    @PostMapping("/groups/homework/{homeworkId}/progress")
    public StudentsHomeworkProgress getGroupsHomeworkProgress(@RequestHeader("Authorization") String authorization,
                                                              @PathVariable int homeworkId,
                                                              @RequestBody List<String> groupNames) {
        return tokenService.returnIfAdmin(authorization, token -> {
            var presenter = new GroupsHomeworkProgressPresenter();
            getGroupsHomeworkProgressUseCase.execute(new GetGroupsHomeworkProgressUseCase.Request(homeworkId, groupNames), presenter);
            return presenter.present();
        });
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
        return mapToList(allHomework, HomeworkView::toViewModel);
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


