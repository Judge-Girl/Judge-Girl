package tw.waterball.judgegirl.springboot.exam.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.entities.Homework;
import tw.waterball.judgegirl.examservice.domain.usecases.homework.CreateHomeworkUseCase;
import tw.waterball.judgegirl.examservice.domain.usecases.homework.GetHomeworkUseCase;
import tw.waterball.judgegirl.springboot.exam.view.HomeworkView;

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

