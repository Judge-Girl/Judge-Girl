package tw.waterball.judgegirl.academy.domain.usecases.homework;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.Homework;

import javax.inject.Named;
import java.util.List;

/**
 * @author swshawnwu@gmail.com(ShawnWu)
 */

@Named
@AllArgsConstructor
public class GetAllHomeworkUseCase {

    private final HomeworkRepository homeworkRepository;

    public void execute(GetAllHomeworkUseCase.Presenter presenter) throws NotFoundException {
        presenter.showAllHomework(homeworkRepository.findAllHomework());
    }

    public interface Presenter {

        void showAllHomework(List<Homework> allHomework);

    }

}
