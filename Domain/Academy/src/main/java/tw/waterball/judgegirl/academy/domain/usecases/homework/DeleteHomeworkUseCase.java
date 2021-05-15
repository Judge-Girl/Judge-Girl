package tw.waterball.judgegirl.academy.domain.usecases.homework;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;

import javax.inject.Named;

/**
 * @author swshawnwu@gmail.com(ShawnWu)
 */

@Named
@AllArgsConstructor
public class DeleteHomeworkUseCase {

    private final HomeworkRepository homeworkRepository;

    public void execute(int homeworkId) {
        homeworkRepository.deleteHomeworkById(homeworkId);
    }

}
