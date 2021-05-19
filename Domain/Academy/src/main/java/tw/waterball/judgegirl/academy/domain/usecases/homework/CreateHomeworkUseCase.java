package tw.waterball.judgegirl.academy.domain.usecases.homework;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.HomeworkRepository;
import tw.waterball.judgegirl.primitives.Homework;
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
        List<Integer> problemIds = request.problemIds
                .stream()
                .filter(this::isProblemExists)
                .collect(Collectors.toList());
        Homework homework = new Homework(name, problemIds);
        homework.validate();
        presenter.showHomework(homeworkRepository.save(homework));
    }

    private boolean isProblemExists(int problemId) {
        return problemServiceDriver.getProblem(problemId).isPresent();
    }

    public interface Presenter {

        void showHomework(Homework homework);

    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        public String name;
        public List<Integer> problemIds = new ArrayList<>();
    }

}
