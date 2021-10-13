package tw.waterball.judgegirl.academy.domain.usecases.homework;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.primitives.Homework;

import javax.inject.Named;
import java.util.List;

import static tw.waterball.judgegirl.commons.utils.StreamUtils.filterToList;

/**
 * @author - sh91013@gmail.com (gordon.liao)
 */
@Named
public class DeleteHomeworkProblemsUseCase extends AbstractHomeworkUseCase {

    public DeleteHomeworkProblemsUseCase(HomeworkRepository homeworkRepository) {
        super(homeworkRepository);
    }

    public void execute(Request request) throws NotFoundException {
        Homework homework = findHomework(request.homeworkId);
        List<Integer> deletedProblemIds = filterToList(request.problemIds, problemId -> homework.containsProblemId(problemId));
        if (!deletedProblemIds.isEmpty()) {
            homework.deleteProblemIds(deletedProblemIds);
            homeworkRepository.save(homework);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public int homeworkId;
        public Integer[] problemIds;
    }

}
