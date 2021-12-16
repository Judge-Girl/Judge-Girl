package tw.waterball.judgegirl.academy.domain.usecases.exam;

import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import javax.inject.Named;
import java.util.Optional;

@Named
public class GetExamOverviewUseCase extends AbstractExamUseCase {
    private final ProblemServiceDriver problemService;

    public GetExamOverviewUseCase(ExamRepository examRepository, ProblemServiceDriver problemService) {
        super(examRepository);
        this.problemService = problemService;
    }

    public void execute(int examId, Presenter presenter) {
        Exam exam = findExam(examId);
        presenter.showExam(exam);
        for (Question question : exam.getQuestions()) {
            findProblem(question)
                    .ifPresentOrElse(problem -> presenter.showQuestion(question, problem),
                            () -> presenter.showNotFoundQuestion(question));
        }
    }

    private Optional<Problem> findProblem(Question question) {
        return problemService.getProblem(question.getId().getProblemId())
                .map(ProblemView::toEntity);
    }

    public interface Presenter {
        void showExam(Exam exam);

        void showQuestion(Question question, Problem problem);

        void showNotFoundQuestion(Question question);
    }

}
