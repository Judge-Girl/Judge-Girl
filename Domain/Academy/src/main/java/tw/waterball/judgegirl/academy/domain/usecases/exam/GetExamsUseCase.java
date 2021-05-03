package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.academy.domain.repositories.ExamFilter;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;

import javax.inject.Named;
import java.util.List;

@Named
@AllArgsConstructor
public class GetExamsUseCase {
    private final ExamRepository examRepository;

    public void execute(ExamFilter filter, @NotNull Presenter presenter) {
        List<Exam> exams = examRepository.findExams(filter);
        presenter.showExams(exams);
    }

    public interface Presenter {
        void showExams(List<Exam> exams);
    }

}
