package tw.waterball.judgegirl.examservice.usecases;

import lombok.AllArgsConstructor;
import lombok.Data;
import tw.waterball.judgegirl.commons.exceptions.NotFoundException;
import tw.waterball.judgegirl.entities.Exam;
import tw.waterball.judgegirl.entities.Question;
import tw.waterball.judgegirl.examservice.repositories.ExamRepository;
import tw.waterball.judgegirl.problemapi.clients.ProblemServiceDriver;
import tw.waterball.judgegirl.problemapi.views.ProblemView;

import javax.inject.Named;

@Named
@AllArgsConstructor
public class GetExamUseCase {
    private final ExamRepository examRepository;
    private final ProblemServiceDriver problemServiceDriver;

    public void execute(Request request, Presenter presenter) {
        Exam exam = examRepository.findById(request.examId).orElseThrow(NotFoundException::new);
        presenter.setExam(exam);
        for (Question question : exam.getQuestions()) {
            ProblemView problemView = problemServiceDriver.getProblem(question.getId().getProblemId());
            presenter.addProblems(problemView);
        }
    }

    public interface Presenter {
        void setExam(Exam exam);

        void addProblems(ProblemView problemView);
    }

    @Data
    @AllArgsConstructor
    public static class Request {
        public int examId;
    }
}
