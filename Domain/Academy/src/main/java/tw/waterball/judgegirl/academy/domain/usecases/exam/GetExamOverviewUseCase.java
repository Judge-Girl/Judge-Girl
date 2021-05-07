package tw.waterball.judgegirl.academy.domain.usecases.exam;

import lombok.AllArgsConstructor;
import tw.waterball.judgegirl.academy.domain.repositories.ExamRepository;
import tw.waterball.judgegirl.primitives.exam.Exam;
import tw.waterball.judgegirl.primitives.exam.Question;
import tw.waterball.judgegirl.primitives.problem.Problem;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;

import javax.inject.Named;

import static tw.waterball.judgegirl.commons.exceptions.NotFoundException.notFound;
import static tw.waterball.judgegirl.problemapi.views.ProblemView.toEntity;

@Named
@AllArgsConstructor
public class GetExamOverviewUseCase {
    private final ExamRepository examRepository;
    private final ProblemServiceDriver problemService;

    public void execute(Request request, Presenter presenter) {
        Exam exam = findExam(request);
        presenter.showExam(exam);

        exam.foreachQuestion(question -> {
            Problem problem = findProblem(question);
            presenter.showQuestion(question, problem);
        });
    }

    private Exam findExam(Request request) {
        return examRepository.findById(request.examId)
                .orElseThrow(() -> notFound(Exam.class).id(request.examId));
    }

    private Problem findProblem(Question question) {
        return toEntity(problemService.getProblem(question.getId().getProblemId()));
    }

    public interface Presenter {
        void showExam(Exam exam);

        void showQuestion(Question question, Problem problem);

    }

    @AllArgsConstructor
    public static class Request {
        public int examId;
    }
}
