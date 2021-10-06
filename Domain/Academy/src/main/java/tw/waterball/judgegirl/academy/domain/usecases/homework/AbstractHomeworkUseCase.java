package tw.waterball.judgegirl.academy.domain.usecases.homework;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.primitives.Homework;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;

/**
 * @author - sh910913@gmail.com (gordon.liao)
 */
@AllArgsConstructor
public abstract class AbstractHomeworkUseCase {
    protected HomeworkRepository homeworkRepository;

    protected Homework findHomework(int homeworkId) {
        return homeworkRepository.findHomeworkById(homeworkId)
                .orElseThrow(() -> notFound(Homework.class).id(homeworkId));
    }
}
