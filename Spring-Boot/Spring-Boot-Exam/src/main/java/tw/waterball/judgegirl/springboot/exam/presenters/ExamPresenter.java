package tw.waterball.judgegirl.springboot.exam.presenters;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.springboot.exam.view.ExamView;


/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@RequestScope
@Component
public class ExamPresenter implements tw.waterball.judgegirl.examservice.domain.usecases.exam.ExamPresenter {
    private ExamView examView;

    @Override
    public void showExam(Exam exam) {
        examView = ExamView.toViewModel(exam);
    }

    public ExamView present() {
        return examView;
    }
}
