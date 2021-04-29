package tw.waterball.judgegirl.examservice.domain.usecases.homework;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Homework;
import tw.waterball.judgegirl.examservice.domain.repositories.HomeworkRepository;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class GetHomeworkUseCase {

    private final HomeworkRepository homeworkRepository;

    public void execute(int homeworkId, Presenter presenter)
            throws NotFoundException {
        Homework homework = homeworkRepository.findHomeworkById(homeworkId)
                .orElseThrow(() -> notFound(Homework.class).id(homeworkId));
        presenter.showHomework(homework);
    }

    public interface Presenter {

        void showHomework(Homework homework);

    }

}
