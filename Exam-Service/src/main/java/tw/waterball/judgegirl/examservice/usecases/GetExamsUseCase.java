package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import tw.waterball.judgegirl.entities.exam.Exam;
import tw.waterball.judgegirl.examservice.repositories.ExamFilter;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;

import javax.inject.Named;
import java.util.List;

@Named
@AllArgsConstructor
public class GetExamsUseCase {
    private final ExamRepository examRepository;

    public void execute(ExamFilter filter, @NotNull Presenter presenter) {
        List<Exam> exams = examRepository.findExams(filter);
        presenter.setExams(exams);
    }

    public interface Presenter {
        void setExams(List<Exam> exams);
    }

}
