package tw.waterball.judgegirl.springboot.academy.presenters;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.springboot.academy.view.ExamView;


/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@RequestScope
@Component
public class ExamPresenter implements tw.waterball.judgegirl.academy.domain.usecases.exam.ExamPresenter {
    private ExamView examView;

    @Override
    public void showExam(Exam exam) {
        examView = ExamView.toViewModel(exam);
    }

    public ExamView present() {
        return examView;
    }
}
