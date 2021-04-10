package tw.waterball.judgegirl.examservice.domain.usecases.homework;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Homework;
import tw.waterball.judgegirl.examservice.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - wally55077@gmail.com
 */
@Named
@AllArgsConstructor
public class CreateHomeworkUseCase {

    private final ProblemServiceDriver problemServiceDriver;

    private final HomeworkRepository homeworkRepository;

    public void execute(Request request, Presenter presenter) {
        String name = request.name;
        String existsProblemIds = request.problemIds.stream()
                .filter(this::isProblemExists)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        Homework homework = new Homework(name, existsProblemIds);
        homework.validate();
        presenter.setHomework(homeworkRepository.save(homework));
    }

    private boolean isProblemExists(int problemId) {
        try {
            problemServiceDriver.getProblem(problemId);
            return true;
        } catch (NotFoundException nfe) {
            return false;
        }
    }

    public interface Presenter {

        void setHomework(Homework homework);

    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public String name;
        public List<Integer> problemIds = new ArrayList<>();
    }

}
